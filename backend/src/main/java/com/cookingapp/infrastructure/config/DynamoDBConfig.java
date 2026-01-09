package com.cookingapp.infrastructure.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DynamoDB設定
 */
@Configuration
public class DynamoDBConfig {

    @Value("${aws.region:ap-northeast-1}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region));

        if (dynamodbEndpoint != null && !dynamodbEndpoint.isEmpty()) {
            // LocalStackを使用する場合は、テスト用の認証情報を使用
            builder.endpointOverride(URI.create(dynamodbEndpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        }
        // それ以外はデフォルトの認証情報チェーン（Lambda IAMロール等）を使用

        return builder.build();
    }
}
