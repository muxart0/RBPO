package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.LicenseType;
import ru.mtuci.demo.services.LicenseTypeService;


import java.util.List;

@RequiredArgsConstructor
@RequestMapping("licensetype")
@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
public class LicenseTypeController {

    private final LicenseTypeService licenseTypeService;

    @GetMapping
    public List<LicenseType> getAll() {
        return licenseTypeService.getAll();
    }

    @GetMapping("/{id}")
    public LicenseType getById(@PathVariable("id") Long id) {
        return licenseTypeService.getLicenseTypeById(id);
    }

    @GetMapping("/name/{name}")
    public LicenseType getByName(@PathVariable("name") String name) {
        return licenseTypeService.getByName(name);
    }

    @PostMapping("/add")
    public void add(@RequestBody LicenseType licenseType) {
        licenseTypeService.add(licenseType);
    }
}
