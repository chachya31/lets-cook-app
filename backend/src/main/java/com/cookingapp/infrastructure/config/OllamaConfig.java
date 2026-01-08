package com.cookingapp.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Ollama設定
 * ローカルLLM実行用の設定
 */
@Configuration
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaConfig {

    private static final Logger log = LoggerFactory.getLogger(OllamaConfig.class);

    @Value("${ollama.baseUrl:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.model:llama3.2}")
    private String model;

    @Bean
    RestTemplate ollamaRestTemplate() {
        log.info("Initializing Ollama REST client with base URL: {}", baseUrl);
        return new RestTemplate();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }
}
