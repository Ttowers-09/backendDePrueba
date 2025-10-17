package com.arsw.ids_ia.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth")
public class OAuth2HelperController {

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    // Helper: 302 to the real oauth2 authorization endpoint
    @GetMapping("/login/google")
    public RedirectView loginWithGoogle() {
        return new RedirectView("/oauth2/authorization/google");
    }

    // Helper: Check if Google client registration is loaded
    @GetMapping("/oidc/status")
    public ResponseEntity<?> oidcStatus() {
        Map<String, Object> body = new HashMap<>();
        boolean present = false;
        String clientId = null;

        if (clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo) {
            ClientRegistration reg = repo.findByRegistrationId("google");
            present = (reg != null);
            if (reg != null) {
                clientId = reg.getClientId();
            }
        }

        body.put("googleClientPresent", present);
        body.put("googleClientId", clientId);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
