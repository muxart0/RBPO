package ru.mtuci.demo.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivationRequest {
    private String activationCode;
    private String deviceInfo;
    private String deviceName;
}