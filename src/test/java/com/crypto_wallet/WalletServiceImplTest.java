package com.crypto_wallet;

import com.crypto_wallet.dto.WalletAssetInfoDTO;
import com.crypto_wallet.dto.WalletInfoDTO;
import com.crypto_wallet.entity.*;
import com.crypto_wallet.exception.ApiException;
import com.crypto_wallet.repository.*;
import com.crypto_wallet.service.impl.WalletServiceImpl;
import com.crypto_wallet.util.CoinCapClient;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletAssetRepository walletAssetRepository;
    @Mock
    private CoinCapClient coinCapClient;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createWallet_shouldCreateWalletForNewEmail() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User user = new User();
        user.setEmail(email);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        User result = walletService.createWallet(email);

        assertEquals(email, result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_shouldThrowExceptionIfWalletExists() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        ApiException ex = assertThrows(ApiException.class, () -> walletService.createWallet(email));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void addAsset_shouldAddNewAssetToWallet() {
        String email = "test@example.com";
        String symbol = "BTC";
        double quantity = 1.0;
        double purchasePrice = 10000.0;

        User user = new User();
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setId(1L);
        asset.setSymbol(symbol);
        asset.setLastPrice(BigDecimal.valueOf(20000));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(coinCapClient.getPrice(symbol)).thenReturn(Mono.just(BigDecimal.valueOf(20000)));
        when(assetRepository.findBySymbol(symbol)).thenReturn(Optional.of(asset));
        when(walletAssetRepository.findByWalletIdAndAssetId(wallet.getId(), asset.getId())).thenReturn(null);
        when(walletAssetRepository.save(any(WalletAsset.class))).thenAnswer(i -> i.getArgument(0));

        WalletAssetInfoDTO dto = walletService.addAsset(email, symbol, quantity, purchasePrice);

        assertEquals(symbol, dto.getSymbol());
        assertEquals(BigDecimal.valueOf(quantity), dto.getQuantity());
        assertEquals(asset.getLastPrice(), dto.getPrice());
        assertEquals(asset.getLastPrice().multiply(BigDecimal.valueOf(quantity)), dto.getValue());
        assertEquals(BigDecimal.valueOf(purchasePrice), dto.getPurchasePrice());
    }

    @Test
    void addAsset_shouldUpdateExistingAssetQuantityAndAveragePrice() {
        String email = "test@example.com";
        String symbol = "BTC";
        double quantity = 1.0;
        double purchasePrice = 10000.0;

        User user = new User();
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        Asset asset = new Asset();
        asset.setId(1L);
        asset.setSymbol(symbol);
        asset.setLastPrice(BigDecimal.valueOf(20000));

        WalletAsset walletAsset = new WalletAsset();
        walletAsset.setWallet(wallet);
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(1.0));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(15000.0));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(coinCapClient.getPrice(symbol)).thenReturn(Mono.just(BigDecimal.valueOf(20000)));
        when(assetRepository.findBySymbol(symbol)).thenReturn(Optional.of(asset));
        when(walletAssetRepository.findByWalletIdAndAssetId(wallet.getId(), asset.getId())).thenReturn(walletAsset);
        when(walletAssetRepository.save(any(WalletAsset.class))).thenAnswer(i -> i.getArgument(0));

        WalletAssetInfoDTO dto = walletService.addAsset(email, symbol, quantity, purchasePrice);

        assertEquals(symbol, dto.getSymbol());
        assertEquals(BigDecimal.valueOf(2.0), dto.getQuantity());
        assertEquals(asset.getLastPrice(), dto.getPrice());
        assertEquals(asset.getLastPrice().multiply(BigDecimal.valueOf(2.0)), dto.getValue());
        // Average price calculation
        BigDecimal expectedAvg = (BigDecimal.valueOf(15000.0).multiply(BigDecimal.valueOf(1.0))
                .add(BigDecimal.valueOf(purchasePrice).multiply(BigDecimal.valueOf(quantity))))
                .divide(BigDecimal.valueOf(2.0), 8, RoundingMode.HALF_UP);
        assertEquals(expectedAvg, dto.getPurchasePrice());
    }

    @Test
    void addAsset_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.addAsset("notfound@example.com", "BTC", 1.0, 10000.0));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void addAsset_shouldThrowExceptionIfWalletNotFound() {
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.addAsset("test@example.com", "BTC", 1.0, 10000.0));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void addAsset_shouldThrowExceptionIfApiPriceNotFound() {
        User user = new User();
        Wallet wallet = new Wallet();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(coinCapClient.getPrice(anyString())).thenReturn(Mono.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.addAsset("test@example.com", "BTC", 1.0, 10000.0));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getWalletInfoDTO_shouldReturnWalletInfo() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        WalletAsset walletAsset = new WalletAsset();
        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setLastPrice(BigDecimal.valueOf(20000));
        walletAsset.setAsset(asset);
        walletAsset.setQuantity(BigDecimal.valueOf(2.0));
        walletAsset.setPurchasePrice(BigDecimal.valueOf(10000));
        wallet.setWalletAssets(Collections.singletonList(walletAsset));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.findAllByWalletId(wallet.getId())).thenReturn(wallet.getWalletAssets());

        WalletInfoDTO dto = walletService.getWalletInfoDTO(email);

        assertEquals(1, dto.getAssets().size());
        assertEquals("BTC", dto.getAssets().get(0).getSymbol());
        assertEquals(BigDecimal.valueOf(2.0), dto.getAssets().get(0).getQuantity());
    }

    @Test
    void getWalletInfoDTO_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.getWalletInfoDTO("notfound@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getWalletInfoDTO_shouldThrowExceptionIfWalletNotFound() {
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> walletService.getWalletInfoDTO("test@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void addAsset_shouldCreateAssetIfNotFound() {
        String email = "test@example.com";
        String symbol = "DOGE";
        double quantity = 2.0;
        double purchasePrice = 0.5;

        User user = new User();
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(coinCapClient.getPrice(symbol)).thenReturn(Mono.just(BigDecimal.valueOf(0.7)));
        when(assetRepository.findBySymbol(symbol)).thenReturn(Optional.empty());
        when(coinCapClient.getName(symbol)).thenReturn(Mono.just("Dogecoin"));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(walletAssetRepository.findByWalletIdAndAssetId(eq(wallet.getId()), any())).thenReturn(null);
        when(walletAssetRepository.save(any(WalletAsset.class))).thenAnswer(i -> i.getArgument(0));

        WalletAssetInfoDTO dto = walletService.addAsset(email, symbol, quantity, purchasePrice);

        assertEquals(symbol, dto.getSymbol());
    }

    @Test
    void addAsset_shouldFallbackToSymbolIfNameNotFound() {
        String email = "test@example.com";
        String symbol = "NEWCOIN";
        double quantity = 1.0;
        double purchasePrice = 1.0;

        User user = new User();
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(coinCapClient.getPrice(symbol)).thenReturn(Mono.just(BigDecimal.valueOf(1.0)));
        when(assetRepository.findBySymbol(symbol)).thenReturn(Optional.empty());
        when(coinCapClient.getName(symbol)).thenReturn(Mono.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(walletAssetRepository.findByWalletIdAndAssetId(eq(wallet.getId()), any())).thenReturn(null);
        when(walletAssetRepository.save(any(WalletAsset.class))).thenAnswer(i -> i.getArgument(0));

        WalletAssetInfoDTO dto = walletService.addAsset(email, symbol, quantity, purchasePrice);

        assertEquals(symbol, dto.getSymbol());
    }

    @Test
    void addAsset_shouldThrowExceptionIfQuantityNotPositive() {
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.addAsset("test@example.com", "BTC", -1, 10000.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void addAsset_shouldThrowExceptionIfPurchasePriceNotPositive() {
        ApiException ex = assertThrows(ApiException.class,
                () -> walletService.addAsset("test@example.com", "BTC", 1.0, -1));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
