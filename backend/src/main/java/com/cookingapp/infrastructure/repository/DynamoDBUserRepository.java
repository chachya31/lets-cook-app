package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.model.User;
import com.cookingapp.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDBUserRepository implements UserRepository {

    private final DynamoDbTable<User> userTable;
    private final String tableName;
    

    public DynamoDBUserRepository(
        DynamoDbEnhancedClient enhancedClient,
        @Value("${aws.dynamodb.table.users:Users}") String tableName) {
        this.tableName = tableName;
        this.userTable = enhancedClient.table(this.tableName, TableSchema.fromBean(User.class));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // Email is not a partition key, so we use scan with filter
        // TODO: Consider adding GSI for Email to improve performance
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("Email = :email")
                        .expressionValues(Map.of(":email", AttributeValue.builder().s(email).build()))
                        .build())
                .build();

        return userTable.scan(scanRequest)
                .items()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findById(String userId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .build();

        User user = userTable.getItem(key);
        return Optional.ofNullable(user);
    }

    @Override
    public User save(User user) {
        userTable.putItem(user);
        return user;
    }
}
