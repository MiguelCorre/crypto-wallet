package com.crypto_wallet.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Wallet Information.
 * This class is for wallet summary (total + list of assets).
 */
@Getter
@Setter
public class WalletInfoDTO {

    private BigDecimal total; // Sum of all asset values
    private List<WalletAssetInfoDTO> assets;
}
