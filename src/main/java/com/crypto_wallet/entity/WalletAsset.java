package com.crypto_wallet.entity;

import static com.crypto_wallet.util.GlobalConstants.PRECISION;
import static com.crypto_wallet.util.GlobalConstants.SCALE;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing the relationship between a wallet and an asset.
 */
@Entity
@Getter
@Setter
public class WalletAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(precision = PRECISION, scale = SCALE, nullable = false)
    private BigDecimal purchasePrice;
}
