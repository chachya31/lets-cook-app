package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.s3.region:ap-northeast-1}")
    private String s3Region;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(s3Endpoint));
            builder.forcePathStyle(true);
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }

        return builder.build();
    }
}
