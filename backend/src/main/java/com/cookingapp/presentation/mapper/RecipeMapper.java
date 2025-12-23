package com.cookingapp.presentation.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;
import com.cookingapp.presentation.dto.StepDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RecipeMapper
 * Recipe エンティティと DTO の変換を担当
 */
public class RecipeMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private RecipeMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * RecipeRequest から Ingredient リストに変換
     */
    public static List<Ingredient> toIngredients(RecipeRequest request) {
        return request.getIngredients().stream()
                .map(IngredientDto::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * RecipeRequest から Step リストに変換
     */
    public static List<Step> toSteps(RecipeRequest request) {
        return request.getSteps().stream()
                .map(StepDto::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * JSON文字列から Ingredient リストに変換（Multipart用）
     */
    public static List<Ingredient> parseIngredients(String json) {
        try {
            List<IngredientDto> dtos = objectMapper.readValue(json, new TypeReference<List<IngredientDto>>() {
            });
            return dtos.stream()
                    .map(IngredientDto::toEntity)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid ingredients JSON format", e);
        }
    }

    /**
     * JSON文字列から Step リストに変換（Multipart用）
     */
    public static List<Step> parseSteps(String json) {
        try {
            List<StepDto> dtos = objectMapper.readValue(json, new TypeReference<List<StepDto>>() {
            });
            return dtos.stream()
                    .map(StepDto::toEntity)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid steps JSON format", e);
        }
    }

    /**
     * Recipe エンティティから RecipeResponse に変換
     */
    public static RecipeResponse toResponse(Recipe recipe) {
        return RecipeResponse.from(recipe);
    }

    /**
     * Recipe エンティティのリストから RecipeResponse のリストに変換
     */
    public static List<RecipeResponse> toResponseList(List<Recipe> recipes) {
        return recipes.stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
