package ru.mtuci.demo.services;

import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.User;

public interface DeviceService {
    Device registerOrUpdateDevice(String deviceInfo, User user, String name);

    Device getDeviceByMac(String mac);
}
