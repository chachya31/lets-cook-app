#!/bin/bash

echo "Seeding test recipes for ingredient search testing..."

export AWS_DEFAULT_REGION=ap-northeast-1

# テスト用ユーザーID
TEST_USER_ID="test-user-001"

# 現在時刻
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S")

# ========================================
# Recipe 1: カレーライス (양파, 감자, 당근, 고기)
# ========================================
echo "Creating Recipe 1: カレーライス..."
RECIPE_ID_1="recipe-test-001"

awslocal dynamodb put-item \
  --table-name cooking-app-recipes-local \
  --item '{
    "RecipeId": {"S": "'$RECIPE_ID_1'"},
    "Title": {"S": "カレーライス"},
    "AuthorId": {"S": "'$TEST_USER_ID'"},
    "CookingTime": {"N": "45"},
    "IsPublic": {"BOOL": true},
    "IsDeleted": {"BOOL": false},
    "CreatedAt": {"S": "'$TIMESTAMP'"},
    "UpdatedAt": {"S": "'$TIMESTAMP'"},
    "Ingredients": {"L": [
      {"M": {"name": {"S": "양파"}, "quantity": {"N": "2"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "감자"}, "quantity": {"N": "3"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "당근"}, "quantity": {"N": "1"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "고기"}, "quantity": {"N": "300"}, "unit": {"S": "g"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "카레루"}, "quantity": {"N": "1"}, "unit": {"S": "박스"}, "optional": {"BOOL": false}}}
    ]},
    "Steps": {"L": [
      {"M": {"description": {"S": "야채를 한입 크기로 자른다"}}},
      {"M": {"description": {"S": "고기를 볶는다"}}},
      {"M": {"description": {"S": "야채를 넣고 볶는다"}}},
      {"M": {"description": {"S": "물을 넣고 끓인다"}}},
      {"M": {"description": {"S": "카레루를 넣고 섞는다"}}}
    ]}
  }'

# Recipe 1의 재료 인덱스
for ingredient in "양파" "감자" "당근" "고기" "카레루"; do
  awslocal dynamodb put-item \
    --table-name cooking-app-recipe-ingredients-local \
    --item '{
      "IngredientName": {"S": "'$ingredient'"},
      "RecipeId": {"S": "'$RECIPE_ID_1'"},
      "RecipeTitle": {"S": "カレーライス"},
      "RecipeImageUrl": {"S": ""}
    }'
done

# ========================================
# Recipe 2: 감자채볶음 (양파, 감자)
# ========================================
echo "Creating Recipe 2: 감자채볶음..."
RECIPE_ID_2="recipe-test-002"

awslocal dynamodb put-item \
  --table-name cooking-app-recipes-local \
  --item '{
    "RecipeId": {"S": "'$RECIPE_ID_2'"},
    "Title": {"S": "감자채볶음"},
    "AuthorId": {"S": "'$TEST_USER_ID'"},
    "CookingTime": {"N": "20"},
    "IsPublic": {"BOOL": true},
    "IsDeleted": {"BOOL": false},
    "CreatedAt": {"S": "'$TIMESTAMP'"},
    "UpdatedAt": {"S": "'$TIMESTAMP'"},
    "Ingredients": {"L": [
      {"M": {"name": {"S": "양파"}, "quantity": {"N": "1"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "감자"}, "quantity": {"N": "2"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "소금"}, "quantity": {"N": "1"}, "unit": {"S": "작은술"}, "optional": {"BOOL": false}}}
    ]},
    "Steps": {"L": [
      {"M": {"description": {"S": "감자를 채썬다"}}},
      {"M": {"description": {"S": "양파를 채썬다"}}},
      {"M": {"description": {"S": "팬에 기름을 두르고 볶는다"}}}
    ]}
  }'

# Recipe 2의 재료 인덱스
for ingredient in "양파" "감자" "소금"; do
  awslocal dynamodb put-item \
    --table-name cooking-app-recipe-ingredients-local \
    --item '{
      "IngredientName": {"S": "'$ingredient'"},
      "RecipeId": {"S": "'$RECIPE_ID_2'"},
      "RecipeTitle": {"S": "감자채볶음"},
      "RecipeImageUrl": {"S": ""}
    }'
done

# ========================================
# Recipe 3: 된장찌개 (양파, 감자, 두부, 된장)
# ========================================
echo "Creating Recipe 3: 된장찌개..."
RECIPE_ID_3="recipe-test-003"

awslocal dynamodb put-item \
  --table-name cooking-app-recipes-local \
  --item '{
    "RecipeId": {"S": "'$RECIPE_ID_3'"},
    "Title": {"S": "된장찌개"},
    "AuthorId": {"S": "'$TEST_USER_ID'"},
    "CookingTime": {"N": "30"},
    "IsPublic": {"BOOL": true},
    "IsDeleted": {"BOOL": false},
    "CreatedAt": {"S": "'$TIMESTAMP'"},
    "UpdatedAt": {"S": "'$TIMESTAMP'"},
    "Ingredients": {"L": [
      {"M": {"name": {"S": "양파"}, "quantity": {"N": "1"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "감자"}, "quantity": {"N": "1"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "두부"}, "quantity": {"N": "1"}, "unit": {"S": "모"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "된장"}, "quantity": {"N": "2"}, "unit": {"S": "큰술"}, "optional": {"BOOL": false}}}
    ]},
    "Steps": {"L": [
      {"M": {"description": {"S": "물을 끓인다"}}},
      {"M": {"description": {"S": "된장을 풀어준다"}}},
      {"M": {"description": {"S": "야채와 두부를 넣고 끓인다"}}}
    ]}
  }'

# Recipe 3의 재료 인덱스
for ingredient in "양파" "감자" "두부" "된장"; do
  awslocal dynamodb put-item \
    --table-name cooking-app-recipe-ingredients-local \
    --item '{
      "IngredientName": {"S": "'$ingredient'"},
      "RecipeId": {"S": "'$RECIPE_ID_3'"},
      "RecipeTitle": {"S": "된장찌개"},
      "RecipeImageUrl": {"S": ""}
    }'
done

# ========================================
# Recipe 4: 김치찌개 (김치, 두부, 고기) - 양파/감자 없음!
# ========================================
echo "Creating Recipe 4: 김치찌개..."
RECIPE_ID_4="recipe-test-004"

awslocal dynamodb put-item \
  --table-name cooking-app-recipes-local \
  --item '{
    "RecipeId": {"S": "'$RECIPE_ID_4'"},
    "Title": {"S": "김치찌개"},
    "AuthorId": {"S": "'$TEST_USER_ID'"},
    "CookingTime": {"N": "25"},
    "IsPublic": {"BOOL": true},
    "IsDeleted": {"BOOL": false},
    "CreatedAt": {"S": "'$TIMESTAMP'"},
    "UpdatedAt": {"S": "'$TIMESTAMP'"},
    "Ingredients": {"L": [
      {"M": {"name": {"S": "김치"}, "quantity": {"N": "200"}, "unit": {"S": "g"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "두부"}, "quantity": {"N": "1"}, "unit": {"S": "모"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "고기"}, "quantity": {"N": "150"}, "unit": {"S": "g"}, "optional": {"BOOL": false}}}
    ]},
    "Steps": {"L": [
      {"M": {"description": {"S": "김치를 볶는다"}}},
      {"M": {"description": {"S": "물을 넣고 끓인다"}}},
      {"M": {"description": {"S": "두부와 고기를 넣는다"}}}
    ]}
  }'

# Recipe 4의 재료 인덱스
for ingredient in "김치" "두부" "고기"; do
  awslocal dynamodb put-item \
    --table-name cooking-app-recipe-ingredients-local \
    --item '{
      "IngredientName": {"S": "'$ingredient'"},
      "RecipeId": {"S": "'$RECIPE_ID_4'"},
      "RecipeTitle": {"S": "김치찌개"},
      "RecipeImageUrl": {"S": ""}
    }'
done

# ========================================
# Recipe 5: 양파링 (양파만!) 
# ========================================
echo "Creating Recipe 5: 양파링..."
RECIPE_ID_5="recipe-test-005"

awslocal dynamodb put-item \
  --table-name cooking-app-recipes-local \
  --item '{
    "RecipeId": {"S": "'$RECIPE_ID_5'"},
    "Title": {"S": "양파링"},
    "AuthorId": {"S": "'$TEST_USER_ID'"},
    "CookingTime": {"N": "15"},
    "IsPublic": {"BOOL": true},
    "IsDeleted": {"BOOL": false},
    "CreatedAt": {"S": "'$TIMESTAMP'"},
    "UpdatedAt": {"S": "'$TIMESTAMP'"},
    "Ingredients": {"L": [
      {"M": {"name": {"S": "양파"}, "quantity": {"N": "2"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "밀가루"}, "quantity": {"N": "100"}, "unit": {"S": "g"}, "optional": {"BOOL": false}}},
      {"M": {"name": {"S": "계란"}, "quantity": {"N": "1"}, "unit": {"S": "개"}, "optional": {"BOOL": false}}}
    ]},
    "Steps": {"L": [
      {"M": {"description": {"S": "양파를 링 모양으로 자른다"}}},
      {"M": {"description": {"S": "튀김옷을 입힌다"}}},
      {"M": {"description": {"S": "기름에 튀긴다"}}}
    ]}
  }'

# Recipe 5의 재료 인덱스
for ingredient in "양파" "밀가루" "계란"; do
  awslocal dynamodb put-item \
    --table-name cooking-app-recipe-ingredients-local \
    --item '{
      "IngredientName": {"S": "'$ingredient'"},
      "RecipeId": {"S": "'$RECIPE_ID_5'"},
      "RecipeTitle": {"S": "양파링"},
      "RecipeImageUrl": {"S": ""}
    }'
done

echo ""
echo "=========================================="
echo "Seed data created successfully!"
echo "=========================================="
echo ""
echo "Test scenarios:"
echo "1. Search '양파' -> 4 recipes (카레, 감자채볶음, 된장찌개, 양파링)"
echo "2. Search '양파' + '감자' -> 3 recipes (카레, 감자채볶음, 된장찌개)"
echo "3. Search '양파' + '감자' + '김치' -> 0 recipes (AND condition)"
echo "4. Search '김치' -> 1 recipe (김치찌개)"
echo "5. Search '두부' -> 2 recipes (된장찌개, 김치찌개)"
echo ""
