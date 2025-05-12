package com.crypto_wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crypto_wallet.entity.User;
import com.crypto_wallet.entity.Wallet;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Wallet entities.
 * This interface extends JpaRepository to provide CRUD operations and custom
 * queries.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User userId);

}
