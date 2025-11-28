package com.cookingapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * DynamoDB設定
 */
@Configuration
public class DynamoDBConfig {

    @Value("${aws.region:ap-northeast-1}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

    @Value("${aws.endpoint:}")
    private String endpoint;

    @Value("${aws.accessKeyId:test}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:test}")
    private String secretAccessKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region));

        // LocalStack用のエンドポイント設定
        String effectiveEndpoint = dynamodbEndpoint != null && !dynamodbEndpoint.isEmpty() 
                ? dynamodbEndpoint 
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
