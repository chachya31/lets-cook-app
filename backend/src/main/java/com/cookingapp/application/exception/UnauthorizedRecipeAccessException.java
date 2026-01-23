package com.cookingapp.application.exception;

public class UnauthorizedRecipeAccessException extends RuntimeException {

    public UnauthorizedRecipeAccessException(String recipeId) {
        super("Not authorized to modify recipe: " + recipeId);
    }
}
