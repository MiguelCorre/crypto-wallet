package com.crypto_wallet.service.impl;

import com.crypto_wallet.dto.WalletAssetInfoDTO;
import com.crypto_wallet.dto.WalletInfoDTO;
import com.crypto_wallet.entity.Asset;
import com.crypto_wallet.entity.User;
import com.crypto_wallet.entity.Wallet;
import com.crypto_wallet.entity.WalletAsset;
import com.crypto_wallet.exception.ApiException;
import com.crypto_wallet.repository.AssetRepository;
import com.crypto_wallet.repository.UserRepository;
import com.crypto_wallet.repository.WalletAssetRepository;
import com.crypto_wallet.repository.WalletRepository;
import com.crypto_wallet.service.WalletService;
import com.crypto_wallet.util.CoinCapClient;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.crypto_wallet.util.GlobalConstants.USER_NOT_FOUND;
import static com.crypto_wallet.util.GlobalConstants.WALLET_NOT_FOUND;
import static com.crypto_wallet.util.GlobalConstants.WALLET_EXISTS;
import static com.crypto_wallet.util.GlobalConstants.ASSET_PRICE_NOT_FOUND;
import static com.crypto_wallet.util.GlobalConstants.QUANTITY_NOT_POSITIVE;
import static com.crypto_wallet.util.GlobalConstants.PURCHASE_PRICE_NOT_POSITIVE;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final WalletRepository walletRepository;
    private final WalletAssetRepository walletAssetRepository;
    private final CoinCapClient coinCapClient;

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    public WalletServiceImpl(UserRepository userRepository, AssetRepository assetRepository,
            WalletRepository walletRepository, WalletAssetRepository walletAssetRepository,
            CoinCapClient coinCapClient) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.walletRepository = walletRepository;
        this.walletAssetRepository = walletAssetRepository;
        this.coinCapClient = coinCapClient;
    }

    @Override
    public User createWallet(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Attempt to create wallet for existing email: {}", email);
            throw new ApiException(WALLET_EXISTS, HttpStatus.CONFLICT);
        }
        User user = new User();
        user.setEmail(email);
        user = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);

        logger.info("Created wallet for user: {}", email);
        return user;
    }

    @Override
    @Transactional
    public WalletAssetInfoDTO addAsset(String email, String symbol, double quantity, double purchasePrice) {
        if (quantity < 0) {
            logger.warn("Attempt to add asset with non-positive quantity: {} for user: {}", quantity, email);
            throw new ApiException(QUANTITY_NOT_POSITIVE, HttpStatus.BAD_REQUEST);
        }
        if (purchasePrice < 0) {
            logger.warn("Attempt to add asset with non-positive purchase price: {} for user: {}", purchasePrice, email);
            throw new ApiException(PURCHASE_PRICE_NOT_POSITIVE, HttpStatus.BAD_REQUEST);
        }

        User user = findUserOrThrow(email);
        Wallet wallet = findWalletOrThrow(user);

        BigDecimal apiPrice = coinCapClient.getPrice(symbol).block();
        if (apiPrice == null) {
            logger.warn("Price not found for symbol: {} (user: {})", symbol, email);
            throw new ApiException(ASSET_PRICE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Asset asset = findOrCreateAsset(symbol, apiPrice);

        WalletAsset walletAsset = walletAssetRepository.findByWalletIdAndAssetId(wallet.getId(), asset.getId());
        if (walletAsset == null) {
            walletAsset = createWalletAsset(wallet, asset, quantity, purchasePrice);
            logger.info("Added new asset {} to wallet of user {}", symbol, email);
        } else {
            updateWalletAsset(walletAsset, quantity, purchasePrice);
            logger.info("Updated asset {} in wallet of user {}", symbol, email);
        }
        walletAssetRepository.save(walletAsset);

        return mapToDto(asset, walletAsset);
    }

    @Override
    public WalletInfoDTO getWalletInfoDTO(String email) {
        User user = findUserOrThrow(email);
        Wallet wallet = findWalletOrThrow(user);
        List<WalletAsset> walletAssets = walletAssetRepository.findAllByWalletId(wallet.getId());

        List<WalletAssetInfoDTO> assetDTOs = walletAssets.stream().map(wa -> {
            WalletAssetInfoDTO dto = new WalletAssetInfoDTO();
            dto.setSymbol(wa.getAsset().getSymbol());
            dto.setPrice(wa.getAsset().getLastPrice());
            dto.setQuantity(wa.getQuantity());
            dto.setValue(wa.getAsset().getLastPrice().multiply(wa.getQuantity()));
            dto.setPurchasePrice(wa.getPurchasePrice());
            return dto;
        }).toList();

        BigDecimal total = assetDTOs.stream()
                .map(WalletAssetInfoDTO::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        WalletInfoDTO walletInfoDTO = new WalletInfoDTO();
        walletInfoDTO.setAssets(assetDTOs);
        walletInfoDTO.setTotal(total);
        return walletInfoDTO;
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Wallet findWalletOrThrow(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(WALLET_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Asset findOrCreateAsset(String symbol, BigDecimal apiPrice) {
        return assetRepository.findBySymbol(symbol)
                .orElseGet(() -> {
                    String name = coinCapClient.getName(symbol).block();
                    Asset newAsset = new Asset();
                    newAsset.setSymbol(symbol);
                    newAsset.setName(name);
                    newAsset.setLastPrice(apiPrice);
                    return assetRepository.save(newAsset);
                });
    }

    private WalletAsset createWalletAsset(Wallet wallet, Asset asset, double quantity, double purchasePrice) {
        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setWallet(wallet);
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(quantity));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(purchasePrice));
        return walletAsset;
    }

    private void updateWalletAsset(WalletAsset walletAsset, double quantity, double purchasePrice) {
        BigDecimal totalCost = walletAsset.getPurchasePrice().multiply(walletAsset.getQuantity())
                .add(BigDecimal.valueOf(purchasePrice).multiply(BigDecimal.valueOf(quantity)));
        BigDecimal newQuantity = walletAsset.getQuantity().add(BigDecimal.valueOf(quantity));
        walletAsset.setQuantity(newQuantity);
        walletAsset.setPurchasePrice(totalCost.divide(newQuantity, 8, RoundingMode.HALF_UP));
    }

    private WalletAssetInfoDTO mapToDto(Asset asset, WalletAsset walletAsset) {
        WalletAssetInfoDTO dto = new WalletAssetInfoDTO();
        dto.setSymbol(asset.getSymbol());
        dto.setPrice(asset.getLastPrice());
        dto.setQuantity(walletAsset.getQuantity());
        dto.setValue(asset.getLastPrice().multiply(walletAsset.getQuantity()));
        dto.setPurchasePrice(walletAsset.getPurchasePrice());
        return dto;
    }
}
