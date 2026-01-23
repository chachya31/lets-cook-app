package com.cookingapp.application.usecase;

import com.cookingapp.application.dto.CreateRecipeInput;
import com.cookingapp.application.dto.RecipeOutput;
import com.cookingapp.application.exception.RecipeNotFoundException;
import com.cookingapp.application.exception.UnauthorizedRecipeAccessException;
import com.cookingapp.application.port.ImageStoragePort;
import com.cookingapp.domain.model.Ingredient;
import com.cookingapp.domain.model.Recipe;
import com.cookingapp.domain.model.Step;
import com.cookingapp.domain.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecipeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RecipeUseCase.class);

    private final RecipeRepository recipeRepository;
    private final ImageStoragePort imageStoragePort;

    public RecipeUseCase(RecipeRepository recipeRepository, ImageStoragePort imageStoragePort) {
        this.recipeRepository = recipeRepository;
        this.imageStoragePort = imageStoragePort;
    }

    public RecipeOutput createRecipe(CreateRecipeInput input, String authorId) {
        logger.debug("Creating recipe for author: {}", authorId);

        String recipeId = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        List<Ingredient> ingredients = input.getIngredients() != null
                ? input.getIngredients().stream()
                    .map(i -> i.toEntity())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        List<Step> steps = input.getSteps() != null
                ? input.getSteps().stream()
                    .map(s -> s.toEntity())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        Recipe recipe = Recipe.builder()
                .recipeId(recipeId)
                .title(input.getTitle())
                .authorId(authorId)
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(input.getCookingTime())
                .isPublic(input.getIsPublic())
                .isDeleted(false)
                .imageUrl(input.getImageKey())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);
        logger.info("Recipe created with ID: {} by author: {}", recipeId, authorId);

        return toOutput(savedRecipe);
    }

    public RecipeOutput getRecipeById(String recipeId, String currentUserId) {
        logger.debug("Getting recipe by ID: {} for user: {}", recipeId, currentUserId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));

        // If recipe is not public, only author can access
        if (!Boolean.TRUE.equals(recipe.getIsPublic()) && !recipe.getAuthorId().equals(currentUserId)) {
            throw new RecipeNotFoundException(recipeId);
        }

        return toOutput(recipe);
    }

    public List<RecipeOutput> getRecipesByAuthor(String authorId, String currentUserId) {
        logger.debug("Getting recipes for author: {} by user: {}", authorId, currentUserId);

        List<Recipe> recipes = recipeRepository.findByAuthor(authorId);

        // If requesting own recipes, return all; otherwise return only public recipes
        if (!authorId.equals(currentUserId)) {
            recipes = recipes.stream()
                    .filter(recipe -> Boolean.TRUE.equals(recipe.getIsPublic()))
                    .collect(Collectors.toList());
        }

        return recipes.stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
    }

    public List<RecipeOutput> getPublicRecipes() {
        logger.debug("Getting all public recipes");

        List<Recipe> recipes = recipeRepository.findAllPublic();

        return recipes.stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
    }

    public RecipeOutput updateRecipe(String recipeId, CreateRecipeInput input, String userId) {
        logger.debug("Updating recipe: {} by user: {}", recipeId, userId);

        Recipe existingRecipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));

        if (!existingRecipe.getAuthorId().equals(userId)) {
            throw new UnauthorizedRecipeAccessException(recipeId);
        }

        List<Ingredient> ingredients = input.getIngredients() != null
                ? input.getIngredients().stream()
                    .map(i -> i.toEntity())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        List<Step> steps = input.getSteps() != null
                ? input.getSteps().stream()
                    .map(s -> s.toEntity())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        String imageKey = input.getImageKey() != null
                ? input.getImageKey()
                : existingRecipe.getImageUrl();

        Recipe updatedRecipe = Recipe.builder()
                .recipeId(recipeId)
                .title(input.getTitle())
                .authorId(existingRecipe.getAuthorId())
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(input.getCookingTime())
                .isPublic(input.getIsPublic())
                .isDeleted(existingRecipe.getIsDeleted())
                .imageUrl(imageKey)
                .createdAt(existingRecipe.getCreatedAt())
                .updatedAt(Instant.now().toString())
                .build();

        Recipe savedRecipe = recipeRepository.save(updatedRecipe);
        logger.info("Recipe updated: {} by user: {}", recipeId, userId);

        return toOutput(savedRecipe);
    }

    public void deleteRecipe(String recipeId, String userId) {
        logger.debug("Deleting recipe: {} by user: {}", recipeId, userId);

        Recipe existingRecipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));

        if (!existingRecipe.getAuthorId().equals(userId)) {
            throw new UnauthorizedRecipeAccessException(recipeId);
        }

        List<String> ingredientNames = existingRecipe.getIngredients() != null
                ? existingRecipe.getIngredients().stream()
                    .map(Ingredient::getName)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        recipeRepository.delete(recipeId, ingredientNames);
        logger.info("Recipe deleted: {} by user: {}", recipeId, userId);
    }

    private RecipeOutput toOutput(Recipe recipe) {
        String presignedUrl = null;
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            presignedUrl = imageStoragePort.generatePresignedUrl(recipe.getImageUrl());
        }
        return RecipeOutput.from(recipe, presignedUrl);
    }
}
