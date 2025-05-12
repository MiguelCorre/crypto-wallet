package com.crypto_wallet;

import com.crypto_wallet.repository.AssetRepository;
import com.crypto_wallet.service.impl.PriceFetchServiceImpl;
import com.crypto_wallet.util.CoinCapClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class PriceFetchServiceImplTests {

    @Mock
    private AssetRepository assetRepository;
    @Mock
    private ThreadPoolTaskExecutor taskExecutor;
    @Mock
    private CoinCapClient coinCapClient;

    @InjectMocks
    private PriceFetchServiceImpl priceFetchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshPrices_shouldInvokeFetchAndSaveForAllSymbols() throws Exception {
        List<String> symbols = List.of("BTC", "ETH");
        when(assetRepository.findAllSymbols()).thenReturn(symbols);

        ThreadPoolExecutor executorService = mock(ThreadPoolExecutor.class);
        when(taskExecutor.getThreadPoolExecutor()).thenReturn(executorService);

        priceFetchService.refreshPrices();

        verify(assetRepository).findAllSymbols();
        verify(taskExecutor.getThreadPoolExecutor()).invokeAll(anyList());
    }

    @Test
    void fetchAndSave_shouldUpdateAssetPriceIfFound() {
        String symbol = "BTC";
        BigDecimal price = BigDecimal.valueOf(10000);
        when(coinCapClient.getPrice(symbol)).thenReturn(reactor.core.publisher.Mono.just(price));
        com.crypto_wallet.entity.Asset asset = new com.crypto_wallet.entity.Asset();
        asset.setSymbol(symbol);
        when(assetRepository.findBySymbol(symbol)).thenReturn(Optional.of(asset));

        priceFetchService.fetchAndSave(symbol);

        verify(assetRepository).save(asset);
        assertEquals(price, asset.getLastPrice());
    }

    @Test
    void fetchAndSave_shouldNotUpdateAssetIfPriceNotFound() {
        String symbol = "BTC";
        when(coinCapClient.getPrice(symbol)).thenReturn(reactor.core.publisher.Mono.empty());

        priceFetchService.fetchAndSave(symbol);

        verify(assetRepository, never()).save(any());
    }
}