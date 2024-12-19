package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.DeviceNotFoundException;
import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.DeviceLicense;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.repo.DeviceLicenseRepository;
import ru.mtuci.demo.repo.DeviceRepository;
import ru.mtuci.demo.services.DeviceLicenseService;
import ru.mtuci.demo.services.DeviceService;
import ru.mtuci.demo.services.LicenseService;

//TODO 1. registerOrUpdateDevice - потенциально любой пользователь может присвоить устройство себе?

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;


    public Device getDeviceByMac(String mac) {
        return deviceRepository.findByMac(mac)
                .orElseThrow(() -> new DeviceNotFoundException("Устройство не найдено"));
    }

    public Device registerOrUpdateDevice(String deviceInfo, User user, String Name) {
        Device device = deviceRepository.findByMac(deviceInfo)
                .orElse(new Device());

        device.setMac(deviceInfo);
        device.setUser(user);
        device.setName(Name);
        return deviceRepository.save(device);
    }
}

