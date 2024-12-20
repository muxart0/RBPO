package ru.mtuci.demo.services;

import ru.mtuci.demo.model.DeviceLicense;

import java.util.List;


public interface DeviceLicenseService {
    void saveDeviceLicense(DeviceLicense deviceLicense);
    List<DeviceLicense> getDeviceLicensesByLicenseId(Long licenseId);
    List<DeviceLicense> getAllLicensesForDevice(Long deviceId);
}
