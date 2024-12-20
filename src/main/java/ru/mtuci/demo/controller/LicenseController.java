package ru.mtuci.demo.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
        LicenseResponse response = licenseService.createLicense(request);
        return ResponseEntity.ok(response);
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

        List<License> activeLicensesForDevice = licenseService.getActiveLicensesForDevice(device, authenticatedUser);


        List<Ticket> tickets = activeLicensesForDevice.stream()
                .map(license -> new Ticket().generateTicket(license, device, authenticatedUser.getId()))
                .toList();

        return ResponseEntity.ok(tickets);
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
        Ticket ticket = licenseService.renewLicense(request, authenticatedUser);
        return ResponseEntity.ok(ticket);
    }



    private User getAuthenticatedUser(HttpServletRequest httpRequest) {
        return userService.getUserByJwt(httpRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
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