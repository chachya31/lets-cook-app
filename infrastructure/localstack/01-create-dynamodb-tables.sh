#!/bin/bash

echo "Creating DynamoDB tables..."

# Users Table
awslocal dynamodb create-table \
  --table-name cooking-app-users-local \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

# Recipes Table
awslocal dynamodb create-table \
  --table-name cooking-app-recipes-local \
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
  --billing-mode PAY_PER_REQUEST

# Schedules Table
awslocal dynamodb create-table \
  --table-name cooking-app-schedules-local \
  --attribute-definitions \
    AttributeName=UserId,AttributeType=S \
    AttributeName=DateRecipeId,AttributeType=S \
  --key-schema \
    AttributeName=UserId,KeyType=HASH \
    AttributeName=DateRecipeId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST

# ShoppingLists Table
awslocal dynamodb create-table \
  --table-name cooking-app-shopping-lists-local \
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
  --billing-mode PAY_PER_REQUEST

# Reviews Table
awslocal dynamodb create-table \
  --table-name cooking-app-reviews-local \
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
  --billing-mode PAY_PER_REQUEST

echo "DynamoDB tables created successfully!"
