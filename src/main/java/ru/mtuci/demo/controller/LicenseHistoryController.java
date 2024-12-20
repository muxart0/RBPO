package ru.mtuci.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.LicenseHistory;
import ru.mtuci.demo.services.LicenseHistoryService;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/license-histories")
public class LicenseHistoryController {

    private final LicenseHistoryService licenseHistoryService;

    public LicenseHistoryController(LicenseHistoryService licenseHistoryService) {
        this.licenseHistoryService = licenseHistoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<LicenseHistory> addLicenseHistory(@RequestBody LicenseHistory licenseHistory) {
        LicenseHistory createdLicenseHistory = licenseHistoryService.addLicenseHistory(licenseHistory);
        return ResponseEntity.ok(createdLicenseHistory);
    }

    @GetMapping
    public ResponseEntity<List<LicenseHistory>> getAllLicenseHistories() {
        List<LicenseHistory> licenseHistories = licenseHistoryService.getAllLicenseHistories();

        return ResponseEntity.ok(licenseHistories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicenseHistory> getLicenseHistoryById(@PathVariable Long id) {
        Optional<LicenseHistory> licenseHistoryOpt = licenseHistoryService.getLicenseHistoryById(id);
        return licenseHistoryOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
