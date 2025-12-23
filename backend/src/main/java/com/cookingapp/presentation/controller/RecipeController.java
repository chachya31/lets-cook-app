package com.cookingapp.presentation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cookingapp.application.usecase.recipe.CreateRecipeUseCase;
import com.cookingapp.application.usecase.recipe.DeleteRecipeUseCase;
import com.cookingapp.application.usecase.recipe.GetRecipeUseCase;
import com.cookingapp.application.usecase.recipe.SearchRecipesUseCase;
import com.cookingapp.application.usecase.recipe.UpdateRecipeUseCase;
import com.cookingapp.application.usecase.recipe.UploadRecipeImageUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;
import com.cookingapp.presentation.mapper.RecipeMapper;

import jakarta.validation.Valid;

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
     * @param keyword  検索キーワード（オプション）
     * @param authorId 作成者ID（オプション）
     * @return レシピリスト
     */
    @GetMapping
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "authorId", required = false) String authorId) {

        List<Recipe> recipes;

        if (authorId != null && !authorId.isEmpty()) {
            recipes = searchRecipesUseCase.executeByAuthor(authorId);
        } else if (keyword != null && !keyword.isEmpty()) {
            recipes = searchRecipesUseCase.executeByKeyword(keyword);
        } else {
            recipes = searchRecipesUseCase.executePublic();
        }

        return ResponseEntity.ok(RecipeMapper.toResponseList(recipes));
    }

    /**
     * レシピ詳細取得
     * GET /api/recipes/{id}
     * 
     * @param id レシピID
     * @return レシピ詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable("id") String id) {
        Recipe recipe = getRecipeUseCase.execute(id);
        return ResponseEntity.ok(RecipeMapper.toResponse(recipe));
    }

    /**
     * レシピ作成
     * POST /api/recipes
     * 
     * @param authorId 作成者ID（ヘッダーから取得）
     * @param request  レシピ作成リクエスト
     * @return 作成されたレシピ
     */
    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(
            @RequestHeader("X-User-Id") String authorId,
            @Valid @RequestBody RecipeRequest request) {

        List<Ingredient> ingredients = RecipeMapper.toIngredients(request);
        List<Step> steps = RecipeMapper.toSteps(request);

        Recipe recipe = createRecipeUseCase.execute(
                authorId,
                request.getTitle(),
                ingredients,
                steps,
                request.getCookingTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeMapper.toResponse(recipe));
    }

    /**
     * レシピ更新
     * PUT /api/recipes/{id}
     * 
     * @param id      レシピID
     * @param userId  ユーザーID（ヘッダーから取得）
     * @param request レシピ更新リクエスト
     * @return 更新されたレシピ
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable("id") String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RecipeRequest request) {

        List<Ingredient> ingredients = RecipeMapper.toIngredients(request);
        List<Step> steps = RecipeMapper.toSteps(request);

        Recipe recipe = updateRecipeUseCase.execute(
                id,
                userId,
                request.getTitle(),
                ingredients,
                steps,
                request.getCookingTime());

        return ResponseEntity.ok(RecipeMapper.toResponse(recipe));
    }

    /**
     * レシピ作成（画像付き）
     * POST /api/recipes/with-images
     * 
     * @param authorId    作成者ID（ヘッダーから取得）
     * @param title       タイトル
     * @param ingredients 食材リスト（JSON文字列）
     * @param steps       手順リスト（JSON文字列）
     * @param cookingTime 調理時間
     * @param mainImage   メイン画像（任意）
     * @param stepImages  手順画像リスト（任意）
     * @return 作成されたレシピ
     */
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> createRecipeWithImages(
            @RequestHeader("X-User-Id") String authorId,
            @RequestParam("title") String title,
            @RequestParam("ingredients") String ingredientsJson,
            @RequestParam("steps") String stepsJson,
            @RequestParam("cookingTime") int cookingTime,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "stepImages", required = false) List<MultipartFile> stepImages) throws IOException {

        List<Ingredient> ingredients = RecipeMapper.parseIngredients(ingredientsJson);
        List<Step> steps = RecipeMapper.parseSteps(stepsJson);

        Recipe recipe = createRecipeUseCase.executeWithImages(
                authorId,
                title,
                ingredients,
                steps,
                cookingTime,
                mainImage,
                stepImages);

        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeMapper.toResponse(recipe));
    }

    /**
     * レシピ削除
     * DELETE /api/recipes/{id}
     * 
     * @param id     レシピID
     * @param userId ユーザーID（ヘッダーから取得）
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable("id") String id,
            @RequestHeader("X-User-Id") String userId) {

        deleteRecipeUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * レシピ画像アップロード
     * POST /api/recipes/{id}/image
     * 
     * @param id     レシピID
     * @param userId ユーザーID（ヘッダーから取得）
     * @param file   画像ファイル
     * @return 更新されたレシピ
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> uploadRecipeImage(
            @PathVariable("id") String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Recipe recipe = uploadRecipeImageUseCase.execute(id, userId, file);
        return ResponseEntity.ok(RecipeMapper.toResponse(recipe));
    }

    /**
     * 手順画像アップロード
     * POST /api/recipes/{id}/steps/{stepIndex}/image
     * 
     * @param id        レシピID
     * @param stepIndex 手順インデックス（0始まり）
     * @param userId    ユーザーID（ヘッダーから取得）
     * @param file      画像ファイル
     * @return 更新されたレシピ
     */
    @PostMapping(value = "/{id}/steps/{stepIndex}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> uploadStepImage(
            @PathVariable("id") String id,
            @PathVariable("stepIndex") int stepIndex,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Recipe recipe = uploadRecipeImageUseCase.executeStepImage(id, userId, stepIndex, file);
        return ResponseEntity.ok(RecipeMapper.toResponse(recipe));
    }
}
