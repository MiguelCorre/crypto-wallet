package com.crypto_wallet.util;

import com.crypto_wallet.entity.Asset;
import com.crypto_wallet.repository.AssetRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class initializes the database with default assets if they do not
 * already exist.
 * It is executed after the application context is initialized.
 */
@Component
public class DataInitializer {

    private final AssetRepository assetRepository;

    public DataInitializer(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @PostConstruct
    public void init() {
        List<String> defaultSymbols = List.of("BTC", "ETH", "DOGE");
        List<String> defaultNames = List.of("bitcoin", "ethereum", "dogecoin");

        for (int i = 0; i < defaultSymbols.size(); i++) {
            String symbol = defaultSymbols.get(i);
            String name = defaultNames.get(i);
            if (!assetRepository.existsBySymbol(symbol)) {
                Asset asset = new Asset();
                asset.setSymbol(symbol);
                asset.setName(name);
                asset.setLastPrice(BigDecimal.ZERO); // Placeholder for initial price
                assetRepository.save(asset);
            }
        }

    }
}
