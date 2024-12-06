package ru.mtuci.demo.services.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivationRequest {
    private String activationCode;
    private String deviceInfo;

}