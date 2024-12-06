package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.exception.ActivationException;
import ru.mtuci.demo.exception.LicenseNotFoundException;
import ru.mtuci.demo.services.LicenseService;
import ru.mtuci.demo.services.impl.ActivationRequest;
import ru.mtuci.demo.ticket.Ticket;

@RestController
@RequestMapping("/activation")
public class ActivationController {

    private final LicenseService licenseService;
    private final JwtTokenProvider jwtTokenProvider;

    public ActivationController(LicenseService licenseService, JwtTokenProvider jwtTokenProvider) {
        this.licenseService = licenseService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(@RequestBody ActivationRequest request, HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT токен отсутствует или некорректен");
        }

        String jwt = authorizationHeader.substring(7); // Убираем "Bearer "
        String username = jwtTokenProvider.getUsername(jwt);

        try {
            Ticket response = licenseService.activateLicense(request, username);
            return ResponseEntity.ok(response);
        } catch (LicenseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ActivationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

