package com.cookingapp.infrastructure.repository;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

/**
 * DynamoDbClientWrapperの実装
 */
@Component
public class DynamoDbClientWrapperImpl implements DynamoDbClientWrapper {
    
    private final DynamoDbClient dynamoDbClient;
    
    public DynamoDbClientWrapperImpl(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }
    
    @Override
    public PutItemResponse putItem(PutItemRequest request) {
        return dynamoDbClient.putItem(request);
    }
    
    @Override
    public GetItemResponse getItem(GetItemRequest request) {
        return dynamoDbClient.getItem(request);
    }
    
    @Override
    public DeleteItemResponse deleteItem(DeleteItemRequest request) {
        return dynamoDbClient.deleteItem(request);
    }
    
    @Override
    public UpdateItemResponse updateItem(UpdateItemRequest request) {
        return dynamoDbClient.updateItem(request);
    }
    
    @Override
    public ScanResponse scan(ScanRequest request) {
        return dynamoDbClient.scan(request);
    }
}
