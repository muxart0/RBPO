package ru.mtuci.demo.services.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.LicenseHistory;
import ru.mtuci.demo.model.User;

import ru.mtuci.demo.repo.LicenseHistoryRepository;
//TODO: ! Предполагаю, что по работе мы поговорим с вами очень подробно

import ru.mtuci.demo.services.LicenseHistoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LicenseHistoryServiceImpl implements LicenseHistoryService {

    private final LicenseHistoryRepository licenseHistoryRepository;

    public LicenseHistoryServiceImpl(LicenseHistoryRepository licenseHistoryRepository) {
        this.licenseHistoryRepository = licenseHistoryRepository;
    }

    @Override
    public void recordLicenseChange(License license, User user, String status, String description) {

        LicenseHistory licenseHistory = new LicenseHistory();
        licenseHistory.setChangeDate(LocalDate.now());
        licenseHistory.setLicense(license);
        licenseHistory.setUser(user);
        licenseHistory.setStatus(status);
        licenseHistory.setDescription(description);

        licenseHistoryRepository.save(licenseHistory);
    }

    public LicenseHistory addLicenseHistory(LicenseHistory licenseHistory) {
        return licenseHistoryRepository.save(licenseHistory);
    }

    public List<LicenseHistory> getAllLicenseHistories() {
        return licenseHistoryRepository.findAll();
    }

    public Optional<LicenseHistory> getLicenseHistoryById(Long id) {
        return licenseHistoryRepository.findById(id);
    }

}
