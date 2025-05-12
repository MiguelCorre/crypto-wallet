package com.crypto_wallet;

import com.crypto_wallet.controller.EvaluationController;
import com.crypto_wallet.dto.WalletEvaluationDTO;
import com.crypto_wallet.service.EvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluationControllerTests {

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private EvaluationController evaluationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWalletEvaluation_shouldReturnEvaluationDTO() {
        String email = "test@example.com";
        LocalDate date = LocalDate.of(2024, 5, 12);
        WalletEvaluationDTO dto = new WalletEvaluationDTO();
        when(evaluationService.getWalletEvaluation(email, date)).thenReturn(dto);

        ResponseEntity<WalletEvaluationDTO> response = evaluationController.getWalletEvaluation(email, date);

        assertEquals(dto, response.getBody());
        verify(evaluationService).getWalletEvaluation(email, date);
    }

    @Test
    void getWalletEvaluation_shouldUseTodayIfDateNotProvided() {
        String email = "test@example.com";
        WalletEvaluationDTO dto = new WalletEvaluationDTO();
        // Simulate null date
        when(evaluationService.getWalletEvaluation(eq(email), any(LocalDate.class))).thenReturn(dto);

        ResponseEntity<WalletEvaluationDTO> response = evaluationController.getWalletEvaluation(email, null);

        assertEquals(dto, response.getBody());
        verify(evaluationService).getWalletEvaluation(eq(email), any(LocalDate.class));
    }
}
