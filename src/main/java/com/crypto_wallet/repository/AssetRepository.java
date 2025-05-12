package com.crypto_wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.crypto_wallet.entity.Asset;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Asset entities.
 * This interface extends JpaRepository to provide CRUD operations and custom
 * queries.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    @Query("SELECT a.symbol FROM Asset a")
    List<String> findAllSymbols();

}
