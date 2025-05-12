package com.crypto_wallet.repository;

import com.crypto_wallet.entity.WalletAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing WalletAsset entities.
 * This interface extends JpaRepository to provide CRUD operations and custom
 * queries.
 */
@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAsset, Long> {
    List<WalletAsset> findAllByWalletId(Long walletId);

    List<WalletAsset> findAllByWallet_User_Email(String email);

    WalletAsset findByWalletIdAndAssetId(Long walletId, Long assetId);
}
