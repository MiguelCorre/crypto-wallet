package com.crypto_wallet;

import com.crypto_wallet.entity.Asset;
import com.crypto_wallet.repository.AssetRepository;
import com.crypto_wallet.util.DataInitializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

class DataInitializerTests {

    @Mock
    private AssetRepository assetRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataInitializer = new DataInitializer(assetRepository);
    }

    @Test
    void init_shouldAddDefaultAssetsIfNotExist() {
        when(assetRepository.existsBySymbol("BTC")).thenReturn(false);
        when(assetRepository.existsBySymbol("ETH")).thenReturn(false);
        when(assetRepository.existsBySymbol("DOGE")).thenReturn(false);

        dataInitializer.init();

        verify(assetRepository, times(1)).save(argThat(asset -> asset.getSymbol().equals("BTC")));
        verify(assetRepository, times(1)).save(argThat(asset -> asset.getSymbol().equals("ETH")));
        verify(assetRepository, times(1)).save(argThat(asset -> asset.getSymbol().equals("DOGE")));
    }

    @Test
    void init_shouldNotDuplicateAssets() {
        when(assetRepository.existsBySymbol("BTC")).thenReturn(true);
        when(assetRepository.existsBySymbol("ETH")).thenReturn(true);
        when(assetRepository.existsBySymbol("DOGE")).thenReturn(true);

        dataInitializer.init();

        verify(assetRepository, never()).save(any(Asset.class));
    }
}
