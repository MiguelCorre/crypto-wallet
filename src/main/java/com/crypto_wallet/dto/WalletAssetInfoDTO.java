package com.crypto_wallet.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Wallet Asset Information.
 * This class is for asset info (symbol, price, quantity, value, purchase
 * price).
 */
@Getter
@Setter
public class WalletAssetInfoDTO {
    private String symbol;
    private BigDecimal price; // Current price per asset
    private BigDecimal quantity; // Amount held
    private BigDecimal value; // price * quantity
    private BigDecimal purchasePrice; // Historical price per asset
}
