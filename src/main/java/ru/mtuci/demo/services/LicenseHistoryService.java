package ru.mtuci.demo.services;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.LicenseHistory;
import ru.mtuci.demo.model.User;

import java.util.List;
import java.util.Optional;

@Service
public interface LicenseHistoryService {
    void recordLicenseChange(License license, User user, String status, String description);

    LicenseHistory addLicenseHistory(LicenseHistory licenseHistory);

    List<LicenseHistory> getAllLicenseHistories();

    Optional<LicenseHistory> getLicenseHistoryById(Long id);

}

