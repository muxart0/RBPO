package ru.mtuci.demo.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.exception.ActivationException;
import ru.mtuci.demo.exception.LicenseNotFoundException;
import ru.mtuci.demo.model.Device;
import ru.mtuci.demo.model.License;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.requests.ActivationRequest;
import ru.mtuci.demo.requests.LicenseCreateRequest;
import ru.mtuci.demo.requests.LicenseRenewalRequest;
import ru.mtuci.demo.services.DeviceService;
import ru.mtuci.demo.services.LicenseService;
import ru.mtuci.demo.services.UserService;
import ru.mtuci.demo.responses.LicenseResponse;
import ru.mtuci.demo.ticket.Ticket;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/licenses")
@RestController
public class LicenseController {
    private final LicenseService licenseService;
    private final UserService userService;
    private final DeviceService deviceService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<License> getAll() {
        return licenseService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<LicenseResponse>  createLicense(
            @RequestBody LicenseCreateRequest request) {
        return licenseService.createLicense(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public void add(@RequestBody License license) {
        licenseService.add(license);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getLicenseInfo(@RequestParam String deviceInfo, HttpServletRequest httpRequest) {
        User authenticatedUser = getAuthenticatedUser(httpRequest);

        Device device = deviceService.getDeviceByMac(deviceInfo);

        License activeLicensesForDevice = licenseService.getActiveLicensesForDevice(device, authenticatedUser);

        Ticket ticket = new Ticket();
        ticket = ticket.generateTicket(activeLicensesForDevice, device, getAuthenticatedUser(httpRequest).getId());

        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(@RequestBody ActivationRequest request,
                                             HttpServletRequest httpRequest) {
        User user = getAuthenticatedUser(httpRequest);

        Ticket response = licenseService.activateLicense(request, user);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/renewal")
    public ResponseEntity<?> renewLicense(@RequestBody LicenseRenewalRequest request, HttpServletRequest httpRequest) {
        User authenticatedUser = getAuthenticatedUser(httpRequest);
        return licenseService.renewLicense(request.getLicenseId(),
                request.getNewActivationKey(), authenticatedUser);
    }



    private User getAuthenticatedUser(HttpServletRequest httpRequest) {
        return userService.getUserByJwt(httpRequest);
    }
    @GetMapping("/{id}")
    public License getById(@PathVariable("id") Long id) {
        return licenseService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/key/{key}")
    public License getByKey(@PathVariable("key") String key) {
        return licenseService.getByKey(key);
    }



}