package ru.mtuci.demo.services.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.DeviceNotFoundException;
import ru.mtuci.demo.model.DeviceLicense;
import ru.mtuci.demo.repo.DeviceLicenseRepository;
import ru.mtuci.demo.services.DeviceLicenseService;


@Service
public class DeviceLicenseServiceImpl implements DeviceLicenseService {
    private final DeviceLicenseRepository deviceLicenseRepository;

    public DeviceLicenseServiceImpl(DeviceLicenseRepository deviceLicenseRepository) {
        this.deviceLicenseRepository = deviceLicenseRepository;
    }

    @Override
    public DeviceLicense getDeviceLicenseByDeviceId(Long id) {
        return deviceLicenseRepository.findByDevice_id(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
    }

    @Override
    public void saveDeviceLicense(DeviceLicense deviceLicense) {
        deviceLicenseRepository.save(deviceLicense);
    }
}
