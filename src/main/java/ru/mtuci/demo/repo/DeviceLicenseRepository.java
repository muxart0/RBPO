package ru.mtuci.demo.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mtuci.demo.model.DeviceLicense;

import java.util.Optional;
import java.util.List;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    Optional<DeviceLicense> findById(Long id);
    @Query("SELECT dl FROM DeviceLicense dl WHERE dl.license.license_id = :licenseId")
    List<DeviceLicense> findAllByLicenseId(@Param("licenseId") Long licenseId);
    List<DeviceLicense> findAllByDeviceId(Long deviceId);

}
