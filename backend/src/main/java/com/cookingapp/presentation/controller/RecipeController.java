package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.*;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ管理コントローラー
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final CreateRecipeUseCase createRecipeUseCase;
    private final UpdateRecipeUseCase updateRecipeUseCase;
    private final DeleteRecipeUseCase deleteRecipeUseCase;
    private final GetRecipeUseCase getRecipeUseCase;
    private final SearchRecipesUseCase searchRecipesUseCase;
    private final UploadRecipeImageUseCase uploadRecipeImageUseCase;

    public RecipeController(
            CreateRecipeUseCase createRecipeUseCase,
            UpdateRecipeUseCase updateRecipeUseCase,
            DeleteRecipeUseCase deleteRecipeUseCase,
            GetRecipeUseCase getRecipeUseCase,
            SearchRecipesUseCase searchRecipesUseCase,
            UploadRecipeImageUseCase uploadRecipeImageUseCase) {
        this.createRecipeUseCase = createRecipeUseCase;
        this.updateRecipeUseCase = updateRecipeUseCase;
        this.deleteRecipeUseCase = deleteRecipeUseCase;
        this.getRecipeUseCase = getRecipeUseCase;
        this.searchRecipesUseCase = searchRecipesUseCase;
        this.uploadRecipeImageUseCase = uploadRecipeImageUseCase;
    }

    /**
     * レシピ検索
     * GET /api/recipes
     * 
     * @param keyword 検索キーワード（オプション）
     * @param authorId 作成者ID（オプション）
     * @return レシピリスト
     */
    @GetMapping
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String authorId) {
        
        List<Recipe> recipes;
        
        if (authorId != null && !authorId.isEmpty()) {
            recipes = searchRecipesUseCase.executeByAuthor(authorId);
        } else if (keyword != null && !keyword.isEmpty()) {
            recipes = searchRecipesUseCase.executeByKeyword(keyword);
        } else {
            recipes = searchRecipesUseCase.executePublic();
        }

        List<RecipeResponse> response = recipes.stream()
                .map(RecipeResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * レシピ詳細取得
     * GET /api/recipes/{id}
     * 
     * @param id レシピID
     * @return レシピ詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable String id) {
        Recipe recipe = getRecipeUseCase.execute(id);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    /**
     * レシピ作成
     * POST /api/recipes
     * 
     * @param authorId 作成者ID（ヘッダーから取得）
     * @param request レシピ作成リクエスト
     * @return 作成されたレシピ
     */
    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(
            @RequestHeader("X-User-Id") String authorId,
            @Valid @RequestBody RecipeRequest request) {
        
        List<Ingredient> ingredients = request.getIngredients().stream()
                .map(dto -> dto.toDomain())
                .collect(Collectors.toList());

        Recipe recipe = createRecipeUseCase.execute(
                authorId,
                request.getTitle(),
                ingredients,
                request.getSteps(),
                request.getCookingTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeResponse.from(recipe));
    }

    /**
     * レシピ更新
     * PUT /api/recipes/{id}
     * 
     * @param id レシピID
     * @param userId ユーザーID（ヘッダーから取得）
     * @param request レシピ更新リクエスト
     * @return 更新されたレシピ
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RecipeRequest request) {
        
        List<Ingredient> ingredients = request.getIngredients().stream()
                .map(dto -> dto.toDomain())
                .collect(Collectors.toList());

        Recipe recipe = updateRecipeUseCase.execute(
                id,
                userId,
                request.getTitle(),
                ingredients,
                request.getSteps(),
                request.getCookingTime()
        );

        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    /**
     * レシピ削除
     * DELETE /api/recipes/{id}
     * 
     * @param id レシピID
     * @param userId ユーザーID（ヘッダーから取得）
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        
        deleteRecipeUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * レシピ画像アップロード
     * POST /api/recipes/{id}/image
     * 
     * @param id レシピID
     * @param userId ユーザーID（ヘッダーから取得）
     * @param file 画像ファイル
     * @return 更新されたレシピ
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> uploadRecipeImage(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        Recipe recipe = uploadRecipeImageUseCase.execute(id, userId, file);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }
}
