package com.crypto_wallet;

import com.crypto_wallet.dto.WalletEvaluationDTO;
import com.crypto_wallet.entity.*;
import com.crypto_wallet.exception.ApiException;
import com.crypto_wallet.repository.UserRepository;
import com.crypto_wallet.repository.WalletAssetRepository;
import com.crypto_wallet.repository.WalletRepository;
import com.crypto_wallet.service.impl.EvaluationServiceImpl;
import com.crypto_wallet.util.CoinCapClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EvaluationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletAssetRepository walletAssetRepository;
    @Mock
    private CoinCapClient coinCapClient;

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWalletEvaluation_shouldReturnCorrectSummaryForToday() {
        String email = "test@example.com";
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        User user = new User();
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setLastPrice(BigDecimal.valueOf(20000));

        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(2));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(10000));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId()))
                .thenReturn(Collections.singletonList(walletAsset));

        WalletEvaluationDTO dto = evaluationService.getWalletEvaluation(email, today);

        assertEquals(BigDecimal.valueOf(40000.00).setScale(2), dto.getTotal());
        assertEquals("BTC", dto.getBestAsset());
        assertEquals("BTC", dto.getWorstAsset());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), dto.getBestPerformance());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), dto.getWorstPerformance());
    }

    @Test
    void getWalletEvaluation_shouldReturnCorrectSummaryForPastDate() {
        String email = "test@example.com";
        LocalDate pastDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);

        User user = new User();
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setName("Bitcoin");

        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(2));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(10000));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId()))
                .thenReturn(Collections.singletonList(walletAsset));
        when(coinCapClient.getHistoricalPrice(eq("Bitcoin"), eq(pastDate)))
                .thenReturn(Mono.just(BigDecimal.valueOf(15000)));

        WalletEvaluationDTO dto = evaluationService.getWalletEvaluation(email, pastDate);

        assertEquals(BigDecimal.valueOf(30000.00).setScale(2), dto.getTotal());
        assertEquals("BTC", dto.getBestAsset());
        assertEquals("BTC", dto.getWorstAsset());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), dto.getBestPerformance());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), dto.getWorstPerformance());
    }

    @Test
    void getWalletEvaluation_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> evaluationService.getWalletEvaluation("notfound@example.com", LocalDate.now()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getWalletEvaluation_shouldThrowExceptionIfWalletNotFound() {
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> evaluationService.getWalletEvaluation("test@example.com", LocalDate.now()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getWalletEvaluation_shouldSkipAssetIfNoHistoricalPrice() {
        String email = "test@example.com";
        LocalDate pastDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);

        User user = new User();
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setName("Bitcoin");

        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(2));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(10000));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId()))
                .thenReturn(Collections.singletonList(walletAsset));
        when(coinCapClient.getHistoricalPrice(eq("Bitcoin"), eq(pastDate))).thenReturn(Mono.empty());

        WalletEvaluationDTO dto = evaluationService.getWalletEvaluation(email, pastDate);

        assertEquals(BigDecimal.ZERO.setScale(2), dto.getTotal());
        assertNull(dto.getBestAsset());
        assertNull(dto.getWorstAsset());
        assertNull(dto.getBestPerformance());
        assertNull(dto.getWorstPerformance());
    }

    @Test
    void getWalletEvaluation_shouldHandleMultipleAssets() {
        String email = "test@example.com";
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        User user = new User();
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset1 = new Asset();
        asset1.setSymbol("BTC");
        asset1.setLastPrice(BigDecimal.valueOf(20000));

        Asset asset2 = new Asset();
        asset2.setSymbol("ETH");
        asset2.setLastPrice(BigDecimal.valueOf(3000));

        WalletAsset walletAsset1 = new WalletAsset();
        walletAsset1.setAsset(asset1);
        walletAsset1.setQuantity(BigDecimal.valueOf(2));
        walletAsset1.setPurchasePrice(BigDecimal.valueOf(10000));

        WalletAsset walletAsset2 = new WalletAsset();
        walletAsset2.setAsset(asset2);
        walletAsset2.setQuantity(BigDecimal.valueOf(5));
        walletAsset2.setPurchasePrice(BigDecimal.valueOf(4000));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId()))
                .thenReturn(List.of(walletAsset1, walletAsset2));

        WalletEvaluationDTO dto = evaluationService.getWalletEvaluation(email, today);

        assertEquals(BigDecimal.valueOf(40000 + 15000).setScale(2), dto.getTotal());
        assertEquals("BTC", dto.getBestAsset()); // 100% gain
        assertEquals("ETH", dto.getWorstAsset()); // -25% loss
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), dto.getBestPerformance());
        assertEquals(BigDecimal.valueOf(-25.00).setScale(2), dto.getWorstPerformance());
    }

    @Test
    void getWalletEvaluation_shouldHandleZeroPurchasePrice() {
        String email = "test@example.com";
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        User user = new User();
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setLastPrice(BigDecimal.valueOf(20000));

        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(2));
        walletAsset.setPurchasePrice(BigDecimal.ZERO); // Zero purchase price

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId()))
                .thenReturn(Collections.singletonList(walletAsset));

        WalletEvaluationDTO dto = evaluationService.getWalletEvaluation(email, today);

        assertEquals(BigDecimal.valueOf(40000.00).setScale(2), dto.getTotal());
        assertEquals("BTC", dto.getBestAsset());
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getBestPerformance());
    }

}
