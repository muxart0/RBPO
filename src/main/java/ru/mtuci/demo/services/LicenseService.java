package ru.mtuci.demo.services;

import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.requests.ActivationRequest;
import ru.mtuci.demo.requests.LicenseCreateRequest;
import ru.mtuci.demo.requests.LicenseRenewalRequest;
import ru.mtuci.demo.responses.LicenseResponse;
import ru.mtuci.demo.ticket.Ticket;

import java.util.List;

public interface LicenseService {
    void add(License license);

    List<License> getAll();

    LicenseResponse createLicense(LicenseCreateRequest request);

    Ticket activateLicense(ActivationRequest request, User user);

    List<License> getActiveLicensesForDevice(Device device, User user);

    License getById(Long id);

    License getByKey(String key);

    Ticket renewLicense(LicenseRenewalRequest request, User user);
}
