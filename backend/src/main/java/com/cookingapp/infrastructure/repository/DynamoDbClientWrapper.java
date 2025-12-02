package com.cookingapp.infrastructure.repository;

import software.amazon.awssdk.services.dynamodb.model.*;

/**
 * DynamoDbClientのラッパーインターフェース
 * テスト可能性を向上させるために導入
 */
public interface DynamoDbClientWrapper {
    
    PutItemResponse putItem(PutItemRequest request);
    
    GetItemResponse getItem(GetItemRequest request);
    
    DeleteItemResponse deleteItem(DeleteItemRequest request);
    
    UpdateItemResponse updateItem(UpdateItemRequest request);
    
    ScanResponse scan(ScanRequest request);
}
