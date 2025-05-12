package com.crypto_wallet.controller;

import com.crypto_wallet.dto.WalletEvaluationDTO;
import com.crypto_wallet.service.EvaluationService;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * EvaluationController handles requests related to wallet evaluations.
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evolution")
    public ResponseEntity<WalletEvaluationDTO> getWalletEvaluation(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        // If date is not provided, use the current date
        if (date == null) {
            date = LocalDate.now(ZoneOffset.UTC);
        }

        // Call the service to get the wallet evaluation
        WalletEvaluationDTO evaluation = evaluationService.getWalletEvaluation(email, date);
        return ResponseEntity.ok(evaluation);
    }
}