package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.ActivationException;
import ru.mtuci.demo.exception.DeviceNotFoundException;
import ru.mtuci.demo.exception.LicenseException;
import ru.mtuci.demo.exception.LicenseNotFoundException;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repo.LicenseRepository;
import ru.mtuci.demo.requests.ActivationRequest;
import ru.mtuci.demo.requests.LicenseCreateRequest;
import ru.mtuci.demo.responses.LicenseResponse;
import ru.mtuci.demo.services.*;
import ru.mtuci.demo.ticket.Ticket;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//TODO: 1. createLicense - Обсуждали на занятии, что deviceCount - максимальное число устройств. Почему оно 0 при создании?
//TODO: 2. ResponseEntity<LicenseResponse> - некорректно использовать в бизнес-логике. Этот тип вынести в контроллер
//TODO: 3. activateLicense - нет разделения первой и повторной активации
//TODO: 4. renewLicense - ключ активации не должен меняться, а тем более приходить извне
//TODO: 5. deviceLicenseService.getDeviceLicenseByDeviceId - в таблице одному id устройства может соответствовать много лицензий, а у вас будто нет

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
        Products product = productService.getProductById(request.getProductId());
        User user = userService.getById(request.getOwnerId());
        LicenseType licenseType = licenseTypeService.getLicenseTypeById(request.getLicenseTypeId());

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

        verifyBlockLicense(license);
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
    public ResponseEntity<Ticket> renewLicense(String deviceInfo, String newActivationKey, User user) {
        Device device = deviceService.getDeviceByMac(deviceInfo);
        DeviceLicense deviceLicense = deviceLicenseService.getDeviceLicenseByDeviceId(device.getId());
        License oldLicense = getById(deviceLicense.getLicense().getLicense_id());

        verifyOldLicense(oldLicense);
        verifyLicenseOwnership(oldLicense, user.getId());

        License newLicense = getByKey(newActivationKey);

        verifyBlockLicense(newLicense);

        verifyLicenseCompatibility(oldLicense, newLicense);

        oldLicense.setDuration(oldLicense.getDuration() + newLicense.getDuration());
        oldLicense.setKey(newActivationKey);
        oldLicense.setEnding_date(oldLicense.getEnding_date().plusDays(newLicense.getDuration()));
        oldLicense.setBlocked(false);

        Ticket ticket = new Ticket();
        ticket = ticket.generateTicket(oldLicense, device, user.getId());

        licenseRepository.save(oldLicense);
        licenseHistoryService.recordLicenseChange(oldLicense, user,"Продлено", "Лицензия успешно продлена");
        licenseRepository.delete(newLicense);

        return ResponseEntity.ok(ticket);
    }

    public License getActiveLicensesForDevice(Device device, User user){
        DeviceLicense deviceLicense = deviceLicenseService.getDeviceLicenseByDeviceId(device.getId());
        return licenseRepository.findById(deviceLicense.getLicense().getLicense_id())
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
