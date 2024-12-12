package ru.mtuci.demo.services;

import ru.mtuci.demo.model.DeviceLicense;


public interface DeviceLicenseService {
    DeviceLicense getDeviceLicenseByDeviceId(Long id);
    void saveDeviceLicense(DeviceLicense deviceLicense);
}
