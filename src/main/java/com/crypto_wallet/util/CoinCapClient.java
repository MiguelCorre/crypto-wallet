package com.crypto_wallet.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * CoinCapClient is a utility class that interacts with the CoinCap API to fetch
 * cryptocurrency prices.
 * It provides methods to get the current price, historical price, and asset
 * name based on the symbol.
 */
@Component
public class CoinCapClient {

    private final WebClient webClient;
    private static final String API_KEY = "16f46ca24e0ff1b9d54ace99fdb4dd7552fb7920bf24c9857f6e0f278add3a3b";
    private static final String API_URL = "https://rest.coincap.io/v3/assets";

    public CoinCapClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(API_URL).build();
    }

    public Mono<BigDecimal> getPrice(String symbol) {
        return webClient.get()
                .uri("?search={token}&limit=1&apiKey={apiKey}", symbol.toLowerCase(), API_KEY)
                .retrieve()
                .bodyToMono(CoinCapResponse.class)
                .flatMap(response -> {
                    if (response != null && response.getData() != null
                            && response.getData().get(0).getPriceUsd() != null) {
                        return Mono.just(new BigDecimal(response.getData().get(0).getPriceUsd()));
                    } else {
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                });
    }

    public Mono<String> getName(String symbol) {
        return webClient.get()
                .uri("?search={token}&limit=1&apiKey={apiKey}", symbol.toLowerCase(), API_KEY)
                .retrieve()
                .bodyToMono(CoinCapResponse.class)
                .flatMap(response -> {
                    if (response != null && response.getData() != null
                            && !response.getData().isEmpty()
                            && response.getData().get(0).getId() != null) {
                        return Mono.just(response.getData().get(0).getId());
                    } else {
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                });
    }

    public Mono<BigDecimal> getHistoricalPrice(String name, LocalDate date) {
        // Convert date to milliseconds since epoch
        long start = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        return webClient.get()
                .uri("/{id}/history?interval=d1&start={start}&end={end}&apiKey={apiKey}",
                        name.toLowerCase(), start, end, API_KEY)
                .retrieve()
                .bodyToMono(CoinCapResponse.class)
                .flatMap(response -> {
                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                        return Mono.just(new BigDecimal(response.getData().get(0).getPriceUsd()));
                    } else {
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> Mono.empty());
    }

    @Getter
    @Setter
    // Inner class to map the JSON response from CoinCap API
    public static class CoinCapResponse {
        private List<Data> data;

        @Getter
        @Setter
        public static class Data {
            private String priceUsd;
            private String id;
            private String time;
        }
    }
}