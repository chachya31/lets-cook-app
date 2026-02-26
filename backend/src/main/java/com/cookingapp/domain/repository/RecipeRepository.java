package com.cookingapp.domain.repository;

import com.cookingapp.domain.entity.Recipe;

import java.util.List;
import java.util.Optional;

/**
 * レシピリポジトリインターフェース
 */
public interface RecipeRepository {
    
    /**
     * レシピを保存
     * 
     * @param recipe レシピ
     * @return 保存されたレシピ
     */
    Recipe save(Recipe recipe);
    
    /**
     * IDでレシピを検索
     * 
     * @param recipeId レシピID
     * @return レシピ（存在しない場合はEmpty）
     */
    Optional<Recipe> findById(String recipeId);
    
    /**
     * 作成者IDでレシピを検索
     * 
     * @param authorId 作成者ID
     * @return レシピリスト
     */
    List<Recipe> findByAuthorId(String authorId);
    
    /**
     * すべての公開レシピを検索
     * 
     * @return レシピリスト
     */
    List<Recipe> findAllPublic();
    
    /**
     * レシピを削除
     * 
     * @param recipeId レシピID
     */
    void delete(String recipeId);
    
    /**
     * レシピが存在するか確認
     * 
     * @param recipeId レシピID
     * @return 存在する場合true
     */
    boolean existsById(String recipeId);
}
