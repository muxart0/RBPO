package ru.mtuci.demo.services.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.DeviceLicense;
import ru.mtuci.demo.repo.DeviceLicenseRepository;
import ru.mtuci.demo.services.DeviceLicenseService;

import java.util.List;


@Service
public class DeviceLicenseServiceImpl implements DeviceLicenseService {
    private final DeviceLicenseRepository deviceLicenseRepository;

    public DeviceLicenseServiceImpl(DeviceLicenseRepository deviceLicenseRepository) {
        this.deviceLicenseRepository = deviceLicenseRepository;
    }

    @Override
    public void saveDeviceLicense(DeviceLicense deviceLicense) {
        deviceLicenseRepository.save(deviceLicense);
    }

    @Override
    public List<DeviceLicense> getDeviceLicensesByLicenseId(Long licenseId) {
        return deviceLicenseRepository.findAllByLicenseId(licenseId);
    }

    @Override
    public List<DeviceLicense> getAllLicensesForDevice(Long deviceId) {
        return deviceLicenseRepository.findAllByDeviceId(deviceId);
    }

}
