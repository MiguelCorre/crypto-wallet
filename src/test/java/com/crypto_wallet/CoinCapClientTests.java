package com.crypto_wallet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.crypto_wallet.util.CoinCapClient;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class CoinCapClientTests {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private WebClient.RequestHeadersSpec headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CoinCapClient coinCapClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        coinCapClient = new CoinCapClient(webClientBuilder);
    }

    @Test
    void getPrice_shouldReturnPriceIfFound() {
        CoinCapClient.CoinCapResponse response = new CoinCapClient.CoinCapResponse();
        CoinCapClient.CoinCapResponse.Data data = new CoinCapClient.CoinCapResponse.Data();
        data.setPriceUsd("12345.67");
        response.setData(java.util.List.of(data));

        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString(), any(), any())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapClient.CoinCapResponse.class)).thenReturn(Mono.just(response));

        BigDecimal price = coinCapClient.getPrice("BTC").block();
        assertEquals(new BigDecimal("12345.67"), price);
    }

    @Test
    void getPrice_shouldReturnEmptyIfNotFound() {
        CoinCapClient.CoinCapResponse response = new CoinCapClient.CoinCapResponse();
        response.setData(java.util.Collections.emptyList());

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapClient.CoinCapResponse.class)).thenReturn(Mono.just(response));

        BigDecimal price = coinCapClient.getPrice("BTC").block();
        assertNull(price);
    }

    @Test
    void getName_shouldReturnNameIfFound() {
        CoinCapClient.CoinCapResponse response = new CoinCapClient.CoinCapResponse();
        CoinCapClient.CoinCapResponse.Data data = new CoinCapClient.CoinCapResponse.Data();
        data.setId("bitcoin");
        response.setData(java.util.List.of(data));

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapClient.CoinCapResponse.class)).thenReturn(Mono.just(response));

        String name = coinCapClient.getName("BTC").block();
        assertEquals("bitcoin", name);
    }

    @Test
    void getHistoricalPrice_shouldReturnPriceIfFound() {
        CoinCapClient.CoinCapResponse response = new CoinCapClient.CoinCapResponse();
        CoinCapClient.CoinCapResponse.Data data = new CoinCapClient.CoinCapResponse.Data();
        data.setPriceUsd("1000.00");
        response.setData(java.util.List.of(data));

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapClient.CoinCapResponse.class)).thenReturn(Mono.just(response));

        BigDecimal price = coinCapClient.getHistoricalPrice("bitcoin", LocalDate.now()).block();
        assertEquals(new BigDecimal("1000.00"), price);
    }

    @Test
    void getHistoricalPrice_shouldReturnEmptyIfNotFound() {
        CoinCapClient.CoinCapResponse response = new CoinCapClient.CoinCapResponse();
        response.setData(java.util.Collections.emptyList());

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapClient.CoinCapResponse.class)).thenReturn(Mono.just(response));

        BigDecimal price = coinCapClient.getHistoricalPrice("bitcoin", LocalDate.now()).block();
        assertNull(price);
    }
}
