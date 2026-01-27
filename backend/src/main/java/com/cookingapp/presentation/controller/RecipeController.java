package com.cookingapp.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.dto.CreateRecipeInput;
import com.cookingapp.application.dto.IngredientInput;
import com.cookingapp.application.dto.RecipeOutput;
import com.cookingapp.application.dto.StepInput;
import com.cookingapp.application.exception.UserNotAuthenticatedException;
import com.cookingapp.application.usecase.RecipeUseCase;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;
import com.cookingapp.presentation.dto.StepDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeUseCase recipeUseCase;

    public RecipeController(RecipeUseCase recipeUseCase) {
        this.recipeUseCase = recipeUseCase;
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(
            @Valid @RequestBody RecipeRequest request,
            Principal principal) {
        String userId = getUserId(principal);
        CreateRecipeInput input = toInput(request);
        RecipeOutput output = recipeUseCase.createRecipe(input, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getRecipes(
            @RequestParam(value = "authorId", required = false) String authorId,
            Principal principal) {
        List<RecipeOutput> outputs;

        if (authorId != null && !authorId.isEmpty()) {
            String currentUserId = principal != null ? principal.getName() : null;
            outputs = recipeUseCase.getRecipesByAuthor(authorId, currentUserId);
        } else {
            outputs = recipeUseCase.getPublicRecipes();
        }

        List<RecipeResponse> responses = outputs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable("id") String id, Principal principal) {
        String currentUserId = principal != null ? principal.getName() : null;
        RecipeOutput output = recipeUseCase.getRecipeById(id, currentUserId);
        return ResponseEntity.ok(toResponse(output));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable("id") String id,
            @Valid @RequestBody RecipeRequest request,
            Principal principal) {
        String userId = getUserId(principal);
        CreateRecipeInput input = toInput(request);
        RecipeOutput output = recipeUseCase.updateRecipe(id, input, userId);
        return ResponseEntity.ok(toResponse(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable("id") String id,
            Principal principal) {
        String userId = getUserId(principal);
        recipeUseCase.deleteRecipe(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Principal principal) {
        if (principal == null) {
            throw new UserNotAuthenticatedException();
        }
        return principal.getName();
    }

    private CreateRecipeInput toInput(RecipeRequest request) {
        List<IngredientInput> ingredients = request.getIngredients() != null
                ? request.getIngredients().stream()
                    .map(this::toIngredientInput)
                    .collect(Collectors.toList())
                : null;

        List<StepInput> steps = request.getSteps() != null
                ? request.getSteps().stream()
                    .map(this::toStepInput)
                    .collect(Collectors.toList())
                : null;

        return CreateRecipeInput.builder()
                .title(request.getTitle())
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(request.getCookingTime())
                .isPublic(request.getIsPublic())
                .imageKey(request.getImageKey())
                .build();
    }

    private IngredientInput toIngredientInput(IngredientDto dto) {
        return IngredientInput.builder()
                .name(dto.getName())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .build();
    }

    private StepInput toStepInput(StepDto dto) {
        return StepInput.builder()
                .stepNumber(dto.getStepNumber())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .videoUrl(dto.getVideoUrl())
                .build();
    }

    private RecipeResponse toResponse(RecipeOutput output) {
        List<IngredientDto> ingredients = output.getIngredients() != null
                ? output.getIngredients().stream()
                    .map(i -> IngredientDto.builder()
                            .name(i.getName())
                            .quantity(i.getQuantity())
                            .unit(i.getUnit())
                            .build())
                    .collect(Collectors.toList())
                : null;

        List<StepDto> steps = output.getSteps() != null
                ? output.getSteps().stream()
                    .map(s -> StepDto.builder()
                            .stepNumber(s.getStepNumber())
                            .description(s.getDescription())
                            .imageUrl(s.getImageUrl())
                            .videoUrl(s.getVideoUrl())
                            .build())
                    .collect(Collectors.toList())
                : null;

        return RecipeResponse.builder()
                .recipeId(output.getRecipeId())
                .title(output.getTitle())
                .authorId(output.getAuthorId())
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(output.getCookingTime())
                .isPublic(output.getIsPublic())
                .imageUrl(output.getImageUrl())
                .createdAt(output.getCreatedAt())
                .updatedAt(output.getUpdatedAt())
                .build();
    }
}
