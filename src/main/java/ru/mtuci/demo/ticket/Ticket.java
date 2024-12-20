package ru.mtuci.demo.ticket;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.License;

//TODO: 1. Метод генерации подписи не является криптографически стойким, так как UUID не предназначен для защиты данных от модификации или подделки

@NoArgsConstructor
@Data
public class Ticket {

    private Date serverDate;
    private Long ticketLifetime;
    private Date activationDate;
    private Date expirationDate;
    private UUID userId;
    private String deviceId;
    private Boolean licenseBlocked;
    private String digitalSignature;
    private Long licenseTypeId;

    public Ticket generateTicket(License license, Device device, Long userId) {
        Ticket ticket = new Ticket();

        ticket.setServerDate(new Date());
        ticket.setTicketLifetime(license.getDuration() * 24L * 60 * 60 * 1000);
        ticket.setActivationDate(java.sql.Date.valueOf(license.getFirst_date_activate()));
        ticket.setExpirationDate(java.sql.Date.valueOf(license.getEnding_date()));
        ticket.setUserId(UUID.nameUUIDFromBytes(userId.toString().getBytes()));
        ticket.setDeviceId(device.getId().toString());
        ticket.setLicenseBlocked(license.getBlocked());
        ticket.setDigitalSignature(generateDigitalSignature(license, device, userId));
        ticket.setLicenseTypeId(license.getLicenseType().getId());

        return ticket;
    }

    private String generateDigitalSignature(License license, Device device, Long userId) {
        try {
            String rawData = license.getKey() + device.getId() + userId + license.getEnding_date();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при генерации цифровой подписи: SHA-256 недоступен.", e);
        }
    }
}
