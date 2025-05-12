package com.crypto_wallet.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

// This configuration class sets up a thread pool for the price fetcher task
@Configuration
public class SchedulerConfig {
    @Bean
    public ThreadPoolTaskExecutor priceFetcherExecutor(@Value("${crypto.update.max-threads}") int maxThreads) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxThreads);
        executor.setMaxPoolSize(maxThreads);
        executor.setThreadNamePrefix("price-fetcher-");
        executor.initialize();
        return executor;
    }
}
