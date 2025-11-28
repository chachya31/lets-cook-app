package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * AWS Cognito設定
 * 実際のAWS Cognitoサービスを使用
 */
@Configuration
public class CognitoConfig {

    @Value("${aws.region:ap-northeast-1}")
    private String region;
    
    @Value("${aws.cognito.region:ap-northeast-1}")
    private String cognitoRegion;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        AwsCredentialsProvider credentialsProvider;
        
        // application.ymlに認証情報が設定されている場合はそれを使用
        if (accessKeyId != null && !accessKeyId.isEmpty() && 
            secretAccessKey != null && !secretAccessKey.isEmpty()) {
            credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        } else {
            // 設定されていない場合はデフォルト（環境変数、~/.aws/credentials等）
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        
        // Cognitoは専用のリージョン設定を使用（LocalStackとは別）
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(cognitoRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
