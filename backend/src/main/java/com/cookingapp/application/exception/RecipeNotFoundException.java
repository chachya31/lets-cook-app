package com.cookingapp.application.exception;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String recipeId) {
        super("Recipe not found with ID: " + recipeId);
    }
}
