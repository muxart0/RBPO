package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.*;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repo.LicenseRepository;
import ru.mtuci.demo.requests.ActivationRequest;
import ru.mtuci.demo.requests.LicenseCreateRequest;
import ru.mtuci.demo.requests.LicenseRenewalRequest;
import ru.mtuci.demo.responses.LicenseResponse;
import ru.mtuci.demo.services.*;
import ru.mtuci.demo.ticket.Ticket;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final ProductService productService;
    private final UserService userService;
    private final LicenseTypeService licenseTypeService;
    private final LicenseHistoryService licenseHistoryService;
    private final DeviceService deviceService;
    private final DeviceLicenseService deviceLicenseService;


    @Override
    public void add(License license) {
        licenseRepository.save(license);
    }

    @Override
    public List<License> getAll() {
        return licenseRepository.findAll();
    }

    @Override
    public License getById(Long id) {
        return licenseRepository.findById(id)
                .orElseThrow(() -> new LicenseException("Лицензия не найдена"));
    }

    @Override
    public License getByKey(String key) {
        return licenseRepository.findByKey(key)
                .orElseThrow(() -> new LicenseException("Лицензия не найдена"));
    }

    @Override
    public LicenseResponse createLicense(LicenseCreateRequest request) {
        Products product = productService.getProductById(request.getProductId());

        verifyBlockProduct(product);

        User user = userService.getById(request.getOwnerId());
        LicenseType licenseType = licenseTypeService.getLicenseTypeById(request.getLicenseTypeId());

        License license = new License();
        license.setProduct(product);
        license.setOwner(user);
        license.setLicenseType(licenseType);
        license.setKey(generateActivationCode());
        license.setDescription(licenseType.getDescription());
        license.setBlocked(false);
        license.setDevice_count(10);
        license.setDuration(licenseType.getDefaultDuration());


        licenseRepository.save(license);

        licenseHistoryService.recordLicenseChange(license, user, "Создано", "Лицензия успешно создана");

        return new LicenseResponse(
            license.getLicense_id(), license.getKey(),
                licenseType.getId(), license.getBlocked(),
                license.getDevice_count(), user.getId(),
                license.getDuration(), license.getDescription(),
                product.getId()
        );
    }

    @Override
    public Ticket activateLicense(ActivationRequest request, User user) {
        License license = getByKey(request.getActivationCode());

        verifyBlockLicense(license);
        verifyBlockProduct(license.getProduct());
        verifyLicenseOwnership(license, user.getId());

        Device device = deviceService.registerOrUpdateDevice(request.getDeviceInfo(), user, request.getDeviceName());

        checkDeviceActivationLimit(license);

        verifyDeviceUniqueness(device, license);

        if (license.getUser() == null) {
            license.setUser(user);
            license.setFirst_date_activate(LocalDate.now());
            license.setEnding_date(license.getFirst_date_activate().plusDays(license.getDuration()));
            licenseRepository.save(license);
        }

        DeviceLicense deviceLicense = new DeviceLicense(license, device, license.getFirst_date_activate());
        deviceLicenseService.saveDeviceLicense(deviceLicense);

        licenseHistoryService.recordLicenseChange(
                license,
                user,
                "Активировано",
                license.getUser() == null ? "Лицензия успешно активирована впервые" : "Лицензия успешно активирована повторно"
        );

        Ticket ticket = new Ticket();
        ticket = ticket.generateTicket(license, device, user.getId());
        return ticket;
    }



    @Override
    public Ticket renewLicense(LicenseRenewalRequest request, User user) {

        License oldLicense = getById(request.getLicenseId());
        verifyBlockProduct(oldLicense.getProduct());

        verifyOldLicense(oldLicense);
        verifyLicenseOwnership(oldLicense, user.getId());


        oldLicense.setDuration(oldLicense.getDuration() + oldLicense.getLicenseType().getDefaultDuration());
        oldLicense.setEnding_date(oldLicense.getEnding_date().plusDays(oldLicense.getLicenseType().getDefaultDuration()));


        licenseRepository.save(oldLicense);

        licenseHistoryService.recordLicenseChange(oldLicense, user, "Продлено", "Лицензия успешно продлена");


        Device device = deviceService.getDeviceByMac(request.getDeviceInfo());

        Ticket ticket = new Ticket();
        ticket = ticket.generateTicket(oldLicense, device, user.getId());

        return ticket;
    }


    public List<License> getActiveLicensesForDevice(Device device, User user) {
        List<DeviceLicense> deviceLicenses = deviceLicenseService.getAllLicensesForDevice(device.getId());

        if (deviceLicenses.isEmpty()) {
            throw new LicenseNotFoundException("Лицензии для устройства не найдены.");
        }

        return deviceLicenses.stream()
                .map(deviceLicense -> licenseRepository.findById(deviceLicense.getLicense().getLicense_id())
                        .orElseThrow(() -> new LicenseNotFoundException("Лицензия не найдена")))
                .filter(license -> license.getUser().getId().equals(user.getId()))
                .toList();
    }


    private void verifyLicenseOwnership(License license, Long newUserId) {
        if (license.getUser() != null) {
            Long currentUserId = license.getUser().getId();
            if (!currentUserId.equals(newUserId)) {
                throw new ActivationException("Лицензия принадлежит другому пользователю.");
            }
        }
    }

    public boolean isLicenseBlocked(License license) {
        return license.getBlocked();
    }


    private void verifyBlockLicense(License license) {
        if (isLicenseBlocked(license)) {
            throw new LicenseException("Невозможно продлить на заблокированную лицензию");
        }
    }

    private void verifyOldLicense(License oldLicense) {
        if(oldLicense.getUser() == null){
            throw new LicenseException("Нельзя продлить неактивированную лицензию");
        }

        verifyBlockLicense(oldLicense);

        if (!oldLicense.getEnding_date().isAfter(LocalDate.now())) {
            throw new LicenseException("Нельзя продлить истекшую лицензию");
        }
    }


    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }

    private int countActivatedDevices(License license) {
        List<DeviceLicense> deviceLicenses = deviceLicenseService.getDeviceLicensesByLicenseId(license.getLicense_id());
        return deviceLicenses.size();
    }

    private void checkDeviceActivationLimit(License license) {
        int activatedDevices = countActivatedDevices(license);
        if (activatedDevices >= license.getDevice_count()) {
            throw new LicenseException("Превышено максимальное количество активированных устройств для лицензии");
        }
    }

    private void verifyDeviceUniqueness(Device device, License newLicense) {
        List<DeviceLicense> deviceLicenses = deviceLicenseService.getAllLicensesForDevice(device.getId());


        boolean isProductAlreadyActivated = deviceLicenses.stream()
                .anyMatch(deviceLicense ->
                        deviceLicense.getLicense().getProduct().getId().equals(newLicense.getProduct().getId()));

        if (isProductAlreadyActivated) {
            throw new LicenseException("Устройство уже активировано для данного продукта.");
        }

    }
    private void verifyBlockProduct(Products product) {
        if (product.getIsBlocked()) {
            throw new ProductException("Продукт заблокирован и не может быть использован.");
        }
    }





}
