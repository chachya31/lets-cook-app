package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3設定
 */
@Configuration
public class S3Config {

    @Value("${aws.region:ap-northeast-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${aws.endpoint:}")
    private String endpoint;

    @Value("${aws.accessKeyId:test}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:test}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region));

        // LocalStack用のエンドポイント設定
        String effectiveEndpoint = s3Endpoint != null && !s3Endpoint.isEmpty() 
                ? s3Endpoint 
                : endpoint;
        
        if (effectiveEndpoint != null && !effectiveEndpoint.isEmpty()) {
            // LocalStackを使用する場合は、テスト用の認証情報を使用
            builder.endpointOverride(URI.create(effectiveEndpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")
                    ))
                    .forcePathStyle(true); // LocalStackではパススタイルが必要
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(region));

        // LocalStack用のエンドポイント設定
        String effectiveEndpoint = s3Endpoint != null && !s3Endpoint.isEmpty() 
                ? s3Endpoint 
                : endpoint;
        
        if (effectiveEndpoint != null && !effectiveEndpoint.isEmpty()) {
            // LocalStackを使用する場合は、テスト用の認証情報を使用
            builder.endpointOverride(URI.create(effectiveEndpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")
                    ));
        } else {
            // 実際のAWSを使用する場合は、設定された認証情報を使用
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ));
        }

        return builder.build();
    }
}
