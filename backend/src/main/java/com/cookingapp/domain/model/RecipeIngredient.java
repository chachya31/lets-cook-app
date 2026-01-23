package com.cookingapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Builder
@AllArgsConstructor
public class RecipeIngredient {

    private String ingredientName;
    private String recipeId;
    private String recipeTitle;

    public RecipeIngredient() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("IngredientName")
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("RecipeId")
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    @DynamoDbAttribute("RecipeTitle")
    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }
}
