package com.crypto_wallet.service;

/**
 * Service class for fetching and updating cryptocurrency prices.
 * This class uses a scheduled task to periodically fetch prices from an CoinCap
 * API and update the database.
 */
public interface PriceFetchService {

    void refreshPrices();

    void fetchAndSave(String symbol);
}
