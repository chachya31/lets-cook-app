package com.cookingapp.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 食材検索リクエスト
 */
@Getter
@Setter
@NoArgsConstructor
public class SearchByIngredientsRequest {

    @NotEmpty(message = "食材を1つ以上指定してください")
    private List<String> ingredients;
}
