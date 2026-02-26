package com.cookingapp.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

/**
 * Gemini API設定
 * ローカル環境: 環境変数から直接APIキーを取得
 * AWS環境: Secrets Managerから取得
 */
@Configuration
public class GeminiConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiConfig.class);

    @Value("${gemini.apiKey:}")
    private String apiKeyDirect;

    @Value("${gemini.apiKeySecretArn:}")
    private String apiKeySecretArn;

    @Autowired(required = false)
    private SecretsManagerClient secretsManagerClient;

    @Bean
    Client geminiClient() {
        String apiKey = resolveApiKey();
        return Client.builder().apiKey(apiKey).build();
    }

    private String resolveApiKey() {
        // Secret ARNが設定されている場合はSecrets Managerから取得
        if (apiKeySecretArn != null && !apiKeySecretArn.isEmpty() && secretsManagerClient != null) {
            log.info("Fetching Gemini API key from Secrets Manager");
            return secretsManagerClient.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(apiKeySecretArn)
                            .build())
                    .secretString();
        }
        // それ以外は環境変数から直接取得（ローカル環境）
        log.info("Using Gemini API key from environment variable");
        return apiKeyDirect;
    }
}
