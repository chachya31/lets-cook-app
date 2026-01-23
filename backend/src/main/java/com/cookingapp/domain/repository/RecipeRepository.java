package com.cookingapp.domain.repository;

import com.cookingapp.domain.model.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    Recipe save(Recipe recipe);

    Optional<Recipe> findById(String recipeId);

    List<Recipe> findByAuthor(String authorId);

    List<Recipe> findAllPublic();

    void delete(String recipeId, List<String> ingredientNames);
}
