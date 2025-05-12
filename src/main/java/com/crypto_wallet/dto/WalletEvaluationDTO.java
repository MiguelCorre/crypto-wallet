package com.crypto_wallet.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Wallet Evaluation.
 * This class is for evaluation summary (total, best/worst asset, best/worst
 * performance).
 */
@Getter
@Setter
public class WalletEvaluationDTO {
    private BigDecimal total;
    private String bestAsset;
    private BigDecimal bestPerformance;
    private String worstAsset;
    private BigDecimal worstPerformance;
}
