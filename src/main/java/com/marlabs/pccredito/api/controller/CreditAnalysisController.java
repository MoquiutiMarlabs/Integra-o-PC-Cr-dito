package com.marlabs.pccredito.api.controller;

import com.marlabs.pccredito.api.request.CreditAnalysisRequest;
import com.marlabs.pccredito.application.CreditAnalysisService;
import com.marlabs.pccredito.api.response.CreditAnalysisResponse;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/credit-analyses")
public class CreditAnalysisController {

    private final CreditAnalysisService creditAnalysisService;

    public CreditAnalysisController(
            CreditAnalysisService creditAnalysisService) {

        this.creditAnalysisService = creditAnalysisService;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CreditAnalysisResponse> analyze(
            @Valid
            @RequestBody
            CreditAnalysisRequest request,

            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            )
            String idempotencyKey) {

        CreditAnalysisResponse response =
                creditAnalysisService.analyze(
                        request,
                        idempotencyKey
                );

        return ResponseEntity.ok(response);
    }
}