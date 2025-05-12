package com.crypto_wallet.controller;

import com.crypto_wallet.dto.WalletAssetInfoDTO;
import com.crypto_wallet.dto.WalletInfoDTO;
import com.crypto_wallet.entity.User;
import com.crypto_wallet.service.WalletService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * WalletController handles requests related to wallet operations.
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Creates a new wallet for the user with the given email.
     *
     * @param email the email of the user
     * @return the created User object
     */
    @PostMapping("/create")
    public ResponseEntity<User> createWallet(@RequestParam String email) {
        User user = walletService.createWallet(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Adds an asset to the user's wallet.
     *
     * @param email         the email of the user
     * @param symbol        the symbol of the asset
     * @param quantity      the quantity of the asset
     * @param purchasePrice the purchase price of the asset
     * @return the added WalletAssetInfoDTO object
     */
    @PostMapping("/addAsset")
    public ResponseEntity<WalletAssetInfoDTO> addAsset(
            @RequestParam String email,
            @RequestParam String symbol,
            @RequestParam double quantity,
            @RequestParam double purchasePrice) {
        WalletAssetInfoDTO asset = walletService.addAsset(email, symbol, quantity, purchasePrice);
        return ResponseEntity.ok(asset);
    }

    /**
     * Removes an asset from the user's wallet.
     *
     * @param email  the email of the user
     * @param symbol the symbol of the asset
     * @return a message indicating success or failure
     */
    @GetMapping("/info")
    public ResponseEntity<WalletInfoDTO> getWalletInfo(@RequestParam String email) {
        WalletInfoDTO info = walletService.getWalletInfoDTO(email);
        return ResponseEntity.ok(info);
    }
}