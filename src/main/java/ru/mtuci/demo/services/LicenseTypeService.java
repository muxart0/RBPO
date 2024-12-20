package ru.mtuci.demo.services;


import ru.mtuci.demo.model.LicenseType;

import java.util.List;

public interface LicenseTypeService {
    LicenseType getLicenseTypeById(Long id);
    List<LicenseType> getAll();
    void add(LicenseType licenseType);
    LicenseType getByName(String name);

}

