package com.crypto_wallet.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Wallet Asset Evaluation.
 * This class is for detailed asset evaluation (historical/current price, value,
 * performance).
 */
@Getter
@Setter
public class WalletAssetEvaluationDTO {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal historicalPrice;
    private BigDecimal currentPrice;
    private BigDecimal value; // quantity * currentPrice
    private BigDecimal performance; // percentage change
}
