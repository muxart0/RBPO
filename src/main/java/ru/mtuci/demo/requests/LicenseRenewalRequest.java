package ru.mtuci.demo.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LicenseRenewalRequest {
    private Long licenseId;
    private String newActivationKey;
}