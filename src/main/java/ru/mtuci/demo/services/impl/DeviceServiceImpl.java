package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.DeviceException;
import ru.mtuci.demo.exception.DeviceNotFoundException;
import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.repo.DeviceRepository;
import ru.mtuci.demo.services.DeviceService;

//TODO 1. registerOrUpdateDevice - потенциально любой пользователь может присвоить устройство себе?

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;


    public Device getDeviceByMac(String mac) {
        return deviceRepository.findByMac(mac)
                .orElseThrow(() -> new DeviceNotFoundException("Устройство не найдено"));
    }

    public Device registerOrUpdateDevice(String deviceInfo, User user, String name) {
        Device device = deviceRepository.findByMac(deviceInfo)
                .orElse(new Device());

        if (device.getId() != null) {
            if (!device.getUser().getId().equals(user.getId())) {
                throw new DeviceException("Устройство принадлежит другому пользователю.");
            }
        }

        device.setMac(deviceInfo);
        device.setUser(user);
        device.setName(name);
        return deviceRepository.save(device);
    }

}

