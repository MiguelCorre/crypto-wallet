package com.crypto_wallet.service;

import com.crypto_wallet.dto.WalletAssetInfoDTO;
import com.crypto_wallet.dto.WalletInfoDTO;
import com.crypto_wallet.entity.User;

/**
 * Service class for managing wallets and assets.
 * This class provides methods to create wallets, add assets, and retrieve
 * wallet information.
 */
public interface WalletService {

    User createWallet(String email);

    WalletAssetInfoDTO addAsset(String email, String symbol, double quantity, double purchasePrice);

    WalletInfoDTO getWalletInfoDTO(String email);
}
