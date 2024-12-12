package ru.mtuci.demo.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@AllArgsConstructor
public class LicenseCreateRequest{
    Long productId;
    Long ownerId;
    Long licenseTypeId;
}
