package com.marlabs.pccredito.api.controller;

import com.marlabs.pccredito.integration.serasa.SerasaTokenManager;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile("dev")
@RestController
@RequestMapping("/internal/dev/serasa")
public class SerasaAuthDevController {

    private final SerasaTokenManager tokenManager;

    public SerasaAuthDevController(
            SerasaTokenManager tokenManager) {

        this.tokenManager = tokenManager;
    }

    @PostMapping("/auth-check")
    public ResponseEntity<Map<String, String>> checkAuthentication() {

        tokenManager.getValidAccessToken();

        return ResponseEntity.ok(
                Map.of("status", "OK")
        );
    }
}