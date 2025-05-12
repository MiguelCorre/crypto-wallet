package com.crypto_wallet.entity;

import static com.crypto_wallet.util.GlobalConstants.PRECISION;
import static com.crypto_wallet.util.GlobalConstants.SCALE;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class representing an asset in the crypto wallet application.
 */
@Entity
@Getter
@Setter
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol; // the token symbol (e.g. BTC, ETH, etc.)

    @Column(nullable = false)
    private String name; // the id used in the CoinCap API to retrieve historical data

    @Column(precision = PRECISION, scale = SCALE)
    private BigDecimal lastPrice;

}