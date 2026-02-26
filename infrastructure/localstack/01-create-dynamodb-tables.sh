#!/bin/bash

echo "Creating DynamoDB tables..."

export AWS_DEFAULT_REGION=ap-northeast-1

# Users Table
awslocal dynamodb create-table \
  --table-name cooking-app-users-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# Recipes Table
awslocal dynamodb create-table \
  --table-name cooking-app-recipes-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=RecipeId,AttributeType=S \
    AttributeName=AuthorId,AttributeType=S \
    AttributeName=CreatedAt,AttributeType=S \
  --key-schema \
    AttributeName=RecipeId,KeyType=HASH \
  --global-secondary-indexes \
    "[
      {
        \"IndexName\": \"GSI_Author\",
        \"KeySchema\": [
          {\"AttributeName\":\"AuthorId\",\"KeyType\":\"HASH\"},
          {\"AttributeName\":\"CreatedAt\",\"KeyType\":\"RANGE\"}
        ],
        \"Projection\": {\"ProjectionType\":\"ALL\"}
      }
    ]" \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# Schedules Table
awslocal dynamodb create-table \
  --table-name cooking-app-schedules-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
    AttributeName=DateRecipeId,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
    AttributeName=DateRecipeId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# ShoppingLists Table
awslocal dynamodb create-table \
  --table-name cooking-app-shopping-lists-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
    AttributeName=ItemId,AttributeType=S \
    AttributeName=NormalizedKey,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
    AttributeName=ItemId,KeyType=RANGE \
  --global-secondary-indexes \
    "[
      {
        \"IndexName\": \"GSI_NormalizedKey\",
        \"KeySchema\": [
          {\"AttributeName\":\"UserId\",\"KeyType\":\"HASH\"},
          {\"AttributeName\":\"NormalizedKey\",\"KeyType\":\"RANGE\"}
        ],
        \"Projection\": {\"ProjectionType\":\"ALL\"}
      }
    ]" \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# Reviews Table
awslocal dynamodb create-table \
  --table-name cooking-app-reviews-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=RecipeId,AttributeType=S \
    AttributeName=ReviewId,AttributeType=S \
    AttributeName=UserId,AttributeType=S \
    AttributeName=CreatedAt,AttributeType=S \
  --key-schema \
    AttributeName=RecipeId,KeyType=HASH \
    AttributeName=ReviewId,KeyType=RANGE \
  --global-secondary-indexes \
    "[
      {
        \"IndexName\": \"GSI_User\",
        \"KeySchema\": [
          {\"AttributeName\":\"UserId\",\"KeyType\":\"HASH\"},
          {\"AttributeName\":\"CreatedAt\",\"KeyType\":\"RANGE\"}
        ],
        \"Projection\": {\"ProjectionType\":\"ALL\"}
      }
    ]" \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# RecipeIngredients Table (Inverted Index for ingredient-based recipe search)
awslocal dynamodb create-table \
  --table-name cooking-app-recipe-ingredients-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=IngredientName,AttributeType=S \
    AttributeName=RecipeId,AttributeType=S \
  --key-schema \
    AttributeName=IngredientName,KeyType=HASH \
    AttributeName=RecipeId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# ChatConversations Table (AI Chat conversation sessions)
awslocal dynamodb create-table \
  --table-name cooking-app-chat-conversations-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
    AttributeName=ConversationId,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
    AttributeName=ConversationId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# ChatMessages Table (AI Chat messages)
awslocal dynamodb create-table \
  --table-name cooking-app-chat-messages-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=ConversationId,AttributeType=S \
    AttributeName=MessageId,AttributeType=S \
  --key-schema \
    AttributeName=ConversationId,KeyType=HASH \
    AttributeName=MessageId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

# Inventory Table (Food inventory management)
awslocal dynamodb create-table \
  --table-name cooking-app-inventory-local \
  --region ap-northeast-1 \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
    AttributeName=ItemId,AttributeType=S \
    AttributeName=ExpiryDate,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
    AttributeName=ItemId,KeyType=RANGE \
  --global-secondary-indexes \
    "[
      {
        \"IndexName\": \"GSI_ExpiryDate\",
        \"KeySchema\": [
          {\"AttributeName\":\"UserId\",\"KeyType\":\"HASH\"},
          {\"AttributeName\":\"ExpiryDate\",\"KeyType\":\"RANGE\"}
        ],
        \"Projection\": {\"ProjectionType\":\"ALL\"}
      }
    ]" \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-1

echo "DynamoDB tables created successfully!"
