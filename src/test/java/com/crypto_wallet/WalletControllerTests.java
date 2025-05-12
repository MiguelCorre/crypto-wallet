package com.crypto_wallet;

import com.crypto_wallet.controller.WalletController;
import com.crypto_wallet.dto.WalletAssetInfoDTO;
import com.crypto_wallet.dto.WalletInfoDTO;
import com.crypto_wallet.entity.User;
import com.crypto_wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTests {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createWallet_shouldReturnUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(walletService.createWallet(email)).thenReturn(user);

        ResponseEntity<User> response = walletController.createWallet(email);

        assertEquals(user, response.getBody());
        verify(walletService).createWallet(email);
    }

    @Test
    void addAsset_shouldReturnAssetInfo() {
        String email = "test@example.com";
        String symbol = "BTC";
        double quantity = 1.0;
        double purchasePrice = 10000.0;
        WalletAssetInfoDTO dto = new WalletAssetInfoDTO();
        dto.setSymbol(symbol);

        when(walletService.addAsset(email, symbol, quantity, purchasePrice)).thenReturn(dto);

        ResponseEntity<WalletAssetInfoDTO> response = walletController.addAsset(email, symbol, quantity, purchasePrice);

        assertEquals(dto, response.getBody());
        verify(walletService).addAsset(email, symbol, quantity, purchasePrice);
    }

    @Test
    void getWalletInfo_shouldReturnWalletInfo() {
        String email = "test@example.com";
        WalletInfoDTO info = new WalletInfoDTO();
        when(walletService.getWalletInfoDTO(email)).thenReturn(info);

        ResponseEntity<WalletInfoDTO> response = walletController.getWalletInfo(email);

        assertEquals(info, response.getBody());
        verify(walletService).getWalletInfoDTO(email);
    }
}
