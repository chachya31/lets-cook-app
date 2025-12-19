package com.cookingapp.infrastructure.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3設定
 */
@Configuration
public class S3Config {

        @Value("${aws.region:ap-northeast-1}")
        private String region;

        @Value("${aws.s3.endpoint:}")
        private String s3Endpoint;

        @Bean
        public S3Client s3Client() {
                var builder = S3Client.builder()
                                .region(Region.of(region));

                if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
                        // LocalStackを使用する場合は、テスト用の認証情報を使用
                        builder.endpointOverride(URI.create(s3Endpoint))
                                        .credentialsProvider(StaticCredentialsProvider.create(
                                                        AwsBasicCredentials.create("test", "test")))
                                        .forcePathStyle(true);
                }
                // それ以外はデフォルトの認証情報チェーン（Lambda IAMロール等）を使用

                return builder.build();
        }

        @Bean
        public S3Presigner s3Presigner() {
                var builder = S3Presigner.builder()
                                .region(Region.of(region));

                if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
                        // LocalStackを使用する場合は、テスト用の認証情報とパススタイルを使用
                        builder.endpointOverride(URI.create(s3Endpoint))
                                        .credentialsProvider(StaticCredentialsProvider.create(
                                                        AwsBasicCredentials.create("test", "test")))
                                        .serviceConfiguration(
                                                        S3Configuration.builder()
                                                                        .pathStyleAccessEnabled(true)
                                                                        .build());
                }
                // それ以外はデフォルトの認証情報チェーン（Lambda IAMロール等）を使用

                return builder.build();
        }
}
