package com.crypto_wallet.util;

/**
 * This class contains global constants used throughout the application.
 * It is designed to be a utility class
 */
public final class GlobalConstants {
    private GlobalConstants() {
    } // Prevent instantiation

    // Error messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String WALLET_NOT_FOUND = "Wallet not found for user";
    public static final String WALLET_EXISTS = "Wallet already exists for this email.";
    public static final String ASSET_PRICE_NOT_FOUND = "Asset price not found in pricing API. Asset not added.";
    public static final String QUANTITY_NOT_POSITIVE = "Quantity must be positive.";
    public static final String PURCHASE_PRICE_NOT_POSITIVE = "Purchase price must be positive.";
    public static final int SCALE = 8;
    public static final int PRECISION = 20;
}
