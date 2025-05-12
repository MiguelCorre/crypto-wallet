package com.crypto_wallet.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.crypto_wallet.repository.AssetRepository;
import com.crypto_wallet.service.PriceFetchService;
import com.crypto_wallet.util.CoinCapClient;

@Service
public class PriceFetchServiceImpl implements PriceFetchService {

    private final AssetRepository assetRepository;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final CoinCapClient coinCapClient;

    private static final Logger logger = LoggerFactory.getLogger(PriceFetchServiceImpl.class);

    public PriceFetchServiceImpl(AssetRepository assetRepository,
            ThreadPoolTaskExecutor taskExecutor, CoinCapClient coinCapClient) {
        this.assetRepository = assetRepository;
        this.taskExecutor = taskExecutor;
        this.coinCapClient = coinCapClient;
    }

    @Scheduled(cron = "${crypto.update.interval-cron}")
    @Override
    public void refreshPrices() {
        logger.info("Starting price fetch...");
        List<String> symbols = assetRepository.findAllSymbols();

        List<Callable<Void>> tasks = symbols.stream()
                .map(symbol -> (Callable<Void>) () -> {
                    fetchAndSave(symbol);
                    return null;
                })
                .toList();

        try {
            taskExecutor.getThreadPoolExecutor().invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void fetchAndSave(String symbol) {
        logger.info("Thread {} is fetching price for {}", Thread.currentThread().getName(), symbol);
        BigDecimal priceOpt = coinCapClient.getPrice(symbol).block();
        if (priceOpt != null) {
            logger.info("Thread {} fetched price {} for {}", Thread.currentThread().getName(), priceOpt, symbol);
            assetRepository.findBySymbol(symbol).ifPresent(asset -> {
                asset.setLastPrice(priceOpt);
                assetRepository.save(asset);
            });
        }
    }
}
