package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * AWS Secrets Manager設定
 * gemini.apiKeySecretArnが設定されている場合のみBean生成
 */
@Configuration
@ConditionalOnProperty(name = "gemini.apiKeySecretArn")
public class SecretsManagerConfig {

    @Value("${aws.region:ap-northeast-1}")
    private String region;

    @Bean
    SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();
    }
}
