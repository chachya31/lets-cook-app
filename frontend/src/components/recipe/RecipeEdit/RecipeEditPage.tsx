import React from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '../../ui/button';
import { Input } from '../../ui/input';
import { Label } from '../../ui/label';
import { Textarea } from '../../ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '../../ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../ui/select';
import { useRecipeEditHandlers } from './useRecipeEditHandlers';
import { unitOptions } from './recipeEditConfig';

/**
 * レシピ編集ページ
 * 表示のみを担当し、ロジックはuseRecipeEditHandlersに委譲
 */
const RecipeEditPage: React.FC = () => {
  const { t } = useTranslation();
  const {
    // 状態
    title,
    setTitle,
    cookingTime,
    setCookingTime,
    ingredients,
    steps,
    isEditMode,
    loading,
    error,

    // イベントハンドラー
    handleAddIngredient,
    handleRemoveIngredient,
    handleIngredientChange,
    handleAddStep,
    handleRemoveStep,
    handleStepChange,
    handleSubmit,
    handleCancel,
  } = useRecipeEditHandlers();

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-6">
        {isEditMode ? t('recipe.edit.title') : t('recipe.create.title')}
      </h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 基本情報 */}
        <Card>
          <CardHeader>
            <CardTitle>{t('recipe.basicInfo')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="title">{t('recipe.title')}</Label>
              <Input
                id="title"
                value={title}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setTitle(e.target.value)}
                required
                maxLength={100}
              />
            </div>
            <div>
              <Label htmlFor="cookingTime">{t('recipe.cookingTime')}</Label>
              <Input
                id="cookingTime"
                type="number"
                value={cookingTime}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setCookingTime(parseInt(e.target.value) || 0)
                }
                required
                min={0}
              />
            </div>
          </CardContent>
        </Card>

        {/* 食材 */}
        <Card>
          <CardHeader>
            <CardTitle>{t('recipe.ingredients')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {ingredients.map((ingredient, index) => (
              <div key={index} className="flex gap-2 items-end">
                <div className="flex-1">
                  <Label>{t('recipe.ingredientName')}</Label>
                  <Input
                    value={ingredient.name}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      handleIngredientChange(index, 'name', e.target.value)
                    }
                    required
                  />
                </div>
                <div className="w-24">
                  <Label>{t('recipe.quantity')}</Label>
                  <Input
                    type="number"
                    value={ingredient.quantity}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      handleIngredientChange(index, 'quantity', parseFloat(e.target.value) || 0)
                    }
                    required
                    min={0}
                    step="0.1"
                  />
                </div>
                <div className="w-32">
                  <Label>{t('recipe.unit')}</Label>
                  <Select
                    value={ingredient.unit}
                    onValueChange={(value) =>
                      handleIngredientChange(index, 'unit', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={t('recipe.selectUnit')} />
                    </SelectTrigger>
                    <SelectContent>
                      {unitOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <Button
                  type="button"
                  variant="destructive"
                  onClick={() => handleRemoveIngredient(index)}
                  disabled={ingredients.length === 1}
                >
                  {t('common.remove')}
                </Button>
              </div>
            ))}
            <Button type="button" variant="outline" onClick={handleAddIngredient}>
              {t('recipe.addIngredient')}
            </Button>
          </CardContent>
        </Card>

        {/* 手順 */}
        <Card>
          <CardHeader>
            <CardTitle>{t('recipe.steps')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {steps.map((step, index) => (
              <div key={index} className="flex gap-2 items-start">
                <span className="font-bold mt-2">{index + 1}.</span>
                <Textarea
                  value={step}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                    handleStepChange(index, e.target.value)
                  }
                  required
                  className="flex-1"
                  rows={2}
                />
                <Button
                  type="button"
                  variant="destructive"
                  onClick={() => handleRemoveStep(index)}
                  disabled={steps.length === 1}
                >
                  {t('common.remove')}
                </Button>
              </div>
            ))}
            <Button type="button" variant="outline" onClick={handleAddStep}>
              {t('recipe.addStep')}
            </Button>
          </CardContent>
        </Card>

        {/* アクションボタン */}
        <div className="flex gap-2 justify-end">
          <Button type="button" variant="outline" onClick={handleCancel}>
            {t('common.cancel')}
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? t('common.saving') : t('common.save')}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default RecipeEditPage;
