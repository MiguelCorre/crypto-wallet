package com.crypto_wallet.service.impl;

import static com.crypto_wallet.util.GlobalConstants.WALLET_NOT_FOUND;
import static com.crypto_wallet.util.GlobalConstants.USER_NOT_FOUND;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.crypto_wallet.dto.WalletEvaluationDTO;
import com.crypto_wallet.entity.User;
import com.crypto_wallet.entity.Wallet;
import com.crypto_wallet.entity.WalletAsset;
import com.crypto_wallet.exception.ApiException;
import com.crypto_wallet.repository.UserRepository;
import com.crypto_wallet.repository.WalletAssetRepository;
import com.crypto_wallet.repository.WalletRepository;
import com.crypto_wallet.service.EvaluationService;
import com.crypto_wallet.util.CoinCapClient;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletAssetRepository walletAssetRepository;
    private final CoinCapClient coinCapClient;

    private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    public EvaluationServiceImpl(UserRepository userRepository, WalletRepository walletRepository,
            WalletAssetRepository walletAssetRepository, CoinCapClient coinCapClient) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletAssetRepository = walletAssetRepository;
        this.coinCapClient = coinCapClient;
    }

    @Override
    public WalletEvaluationDTO getWalletEvaluation(String email, LocalDate date) {
        User user = getUserByEmail(email);
        Wallet wallet = getWalletByUser(user);
        List<WalletAsset> walletAssets = walletAssetRepository.findAllByWalletId(wallet.getId());

        boolean isToday = date.equals(LocalDate.now(ZoneOffset.UTC));

        BigDecimal total = BigDecimal.ZERO;
        String bestAsset = null;
        String worstAsset = null;
        BigDecimal bestPerf = null;
        BigDecimal worstPerf = null;

        for (WalletAsset wa : walletAssets) {
            BigDecimal currentPrice = getAssetPrice(wa, date, isToday);
            if (currentPrice == null) {
                logger.warn("Skipping asset {} for user {}: no price found for date {}", wa.getAsset().getSymbol(),
                        user.getEmail(), date);
                continue;
            }

            BigDecimal purchasePrice = wa.getPurchasePrice();
            BigDecimal performance = calculatePerformance(currentPrice, purchasePrice);
            BigDecimal value = currentPrice.multiply(wa.getQuantity());
            total = total.add(value);

            if (bestPerf == null || performance.compareTo(bestPerf) > 0) {
                bestPerf = performance;
                bestAsset = wa.getAsset().getSymbol();
            }
            if (worstPerf == null || performance.compareTo(worstPerf) < 0) {
                worstPerf = performance;
                worstAsset = wa.getAsset().getSymbol();
            }
        }

        return mapToDto(total, bestAsset, bestPerf, worstAsset, worstPerf);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(WALLET_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private BigDecimal getAssetPrice(WalletAsset wa, LocalDate date, boolean isToday) {
        if (isToday) {
            return wa.getAsset().getLastPrice();
        } else {
            return coinCapClient.getHistoricalPrice(wa.getAsset().getName(), date).block();
        }
    }

    private BigDecimal calculatePerformance(BigDecimal currentPrice, BigDecimal purchasePrice) {
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            return currentPrice.subtract(purchasePrice)
                    .divide(purchasePrice, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    private WalletEvaluationDTO mapToDto(BigDecimal total, String bestAsset, BigDecimal bestPerf, String worstAsset,
            BigDecimal worstPerf) {
        WalletEvaluationDTO result = new WalletEvaluationDTO();
        result.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        result.setBestAsset(bestAsset);
        result.setBestPerformance(bestPerf != null ? bestPerf.setScale(2, RoundingMode.HALF_UP) : null);
        result.setWorstAsset(worstAsset);
        result.setWorstPerformance(worstPerf != null ? worstPerf.setScale(2, RoundingMode.HALF_UP) : null);
        return result;
    }
}
