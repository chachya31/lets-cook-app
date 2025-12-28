package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

/**
 * Gemini API設定
 */
@Configuration
public class GeminiConfig {

    @Value("${gemini.apiKey:}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        return Client.builder().apiKey(apiKey).build();
    }
}
