package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.ActivationException;
import ru.mtuci.demo.exception.DeviceNotFoundException;
import ru.mtuci.demo.exception.LicenseException;
import ru.mtuci.demo.exception.LicenseNotFoundException;
import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.DeviceLicense;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.repo.DeviceLicenseRepository;
import ru.mtuci.demo.repo.LicenseRepository;
import ru.mtuci.demo.requests.ActivationRequest;
import ru.mtuci.demo.requests.LicenseCreateRequest;
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
    public ResponseEntity<LicenseResponse> createLicense(LicenseCreateRequest request) {
        var product = productService.getProductById(request.getProductId());
        var user = userService.getById(request.getOwnerId());
        var licenseType = licenseTypeService.getLicenseTypeById(request.getLicenseTypeId());


        License license = new License();
        license.setProduct(product);
        license.setOwner(user);
        license.setLicenseType(licenseType);
        license.setKey(generateActivationCode());
        license.setDescription(licenseType.getDescription());
        license.setBlocked(false);
        license.setDevice_count(0);
        license.setDuration(licenseType.getDefaultDuration());


        licenseRepository.save(license);

        licenseHistoryService.recordLicenseChange(license, user, "Создано", "Лицензия успешно создана");

        LicenseResponse response = new LicenseResponse(
            license.getLicense_id(), license.getKey(),
                licenseType.getId(), license.getBlocked(),
                license.getDevice_count(), user.getId(),
                license.getDuration(), license.getDescription(),
                product.getId()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public Ticket activateLicense(ActivationRequest request, User user) {
        License license = getByKey(request.getActivationCode());

        if (isLicenseBlocked(license)) {
            throw new ActivationException("Активация невозможна");
        }

        verifyLicenseOwnership(license, user.getId());
        Device device = deviceService.registerOrUpdateDevice(request.getDeviceInfo(),user,request.getDeviceName());
        checkDeviceLicense(device);

        license.setUser(user);
        license.setDevice_count(license.getDevice_count()+1);
        license.setFirst_date_activate(LocalDate.now());
        license.setEnding_date(license.getFirst_date_activate().plusDays(license.getDuration()));

        DeviceLicense deviceLicense = new DeviceLicense(license, device, license.getFirst_date_activate());

        deviceLicenseService.saveDeviceLicense(deviceLicense);
        licenseRepository.save(license);


        licenseHistoryService.recordLicenseChange(license, user,"Активировано", "Лицензия успешно активирована");
        Ticket ticket = new Ticket();
        ticket = ticket.generateTicket(license, device, user.getId());

        return ticket;
    }

    @Override
    public ResponseEntity<LicenseResponse> renewLicense(Long licenseId, String newActivationKey, User user) {
        License oldLicense = getById(licenseId);
        if(oldLicense.getUser() == null){
            throw new LicenseException("Нельзя продлить неактивированную лицензию");
        }
        if (isLicenseBlocked(oldLicense)) {
            throw new LicenseException("Невозможно продлить заблокированную лицензию");
        }

        verifyLicenseOwnership(oldLicense, user.getId());

        License newLicense = getByKey(newActivationKey);

        verifyLicenseCompatibility(oldLicense, newLicense);

        oldLicense.setDuration(oldLicense.getDuration() + newLicense.getDuration());
        oldLicense.setKey(newActivationKey);
        oldLicense.setEnding_date(oldLicense.getEnding_date().plusDays(newLicense.getDuration()));
        oldLicense.setBlocked(false);

        LicenseResponse response = new LicenseResponse(
                oldLicense.getLicense_id(), oldLicense.getKey(),
                oldLicense.getLicenseType().getId(), oldLicense.getBlocked(),
                oldLicense.getDevice_count(), oldLicense.getOwner().getId(),
                oldLicense.getDuration(), oldLicense.getDescription(),
                oldLicense.getProduct().getId()
        );

        licenseRepository.save(oldLicense);
        licenseHistoryService.recordLicenseChange(oldLicense, user,"Продлено", "Лицензия успешно продлена");
        licenseRepository.delete(newLicense);
        return ResponseEntity.ok(response);
    }

    public License getActiveLicensesForDevice(Device device, User user){
        DeviceLicense deviceLicense = deviceLicenseService.getDeviceLicenseByDeviceId(device.getId());
        return licenseRepository.findById(deviceLicense.getDevice().getId())
                .orElseThrow(() -> new LicenseNotFoundException("Лицензия не найдена"));
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

    private void verifyLicenseCompatibility(License oldLicense, License newLicense) {
        if (!oldLicense.getProduct().getId().equals(newLicense.getProduct().getId())) {
            throw new LicenseException("Продукты старой и новой лицензий не совпадают");
        }

        if (!oldLicense.getLicenseType().getId().equals(newLicense.getLicenseType().getId())) {
            throw new LicenseException("Типы лицензий не совпадают");
        }

        if(oldLicense.equals(newLicense)){
            throw new LicenseException("Лицензии совпадают");
        }
    }

    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }

    public void checkDeviceLicense(Device device) {
        DeviceLicense deviceLicense;
        try {
            deviceLicense = deviceLicenseService.getDeviceLicenseByDeviceId(device.getId());
        } catch (Exception e) {
            return;
        }
        License license = getById(deviceLicense.getLicense().getLicense_id());
        if(!license.getBlocked()){
            throw new DeviceNotFoundException("Устройство с таким MAC-адресом и лицензией уже существует");
        }
    }
}
