package ru.mtuci.demo.services.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.LicenseException;
import ru.mtuci.demo.exception.UserException;
import ru.mtuci.demo.model.LicenseType;

import ru.mtuci.demo.repo.LicenseTypeRepository;
import ru.mtuci.demo.services.LicenseTypeService;

import java.util.List;

@Service
public class LicenseTypeServiceImpl implements LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeServiceImpl(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    @Override
    public LicenseType getLicenseTypeById(Long id) {
        return licenseTypeRepository.findById(id)
                .orElseThrow(() -> new LicenseException("License Type not found"));
    }

    @Override
    public List<LicenseType> getAll() {
        return licenseTypeRepository.findAll();
    }

    public void add(LicenseType licenseType) {
        if (licenseTypeRepository.existsByName(licenseType.getName())) {
            throw new IllegalArgumentException("Product with the same name already exists.");
        }
        licenseTypeRepository.save(licenseType);
    }


    @Override
    public LicenseType getByName(String name)  {
        return licenseTypeRepository.findByName(name)
                .orElseThrow(() -> new UserException("User not found"));
    }
}

