import { Trash2 } from 'lucide-react';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useScrollToMessage } from '../../../hooks/useScrollToMessage';
import { ImageUploader } from '../../common/ImageUploader';
import { MessageDisplay } from '../../common/MessageDisplay';
import { Button } from '../../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../ui/card';
import { Input } from '../../ui/input';
import { Label } from '../../ui/label';
import { Textarea } from '../../ui/textarea';
import { useRecipeEditHandlers } from './useRecipeEditHandlers';

/**
 * レシピ編集ページ
 * 表示のみを担当し、ロジックはuseRecipeEditHandlersに委譲
 */
const RecipeEditPage: React.FC = () => {
  const { t } = useTranslation();
  const { messageRef, scrollToMessage } = useScrollToMessage();
  const {
    // 状態
    title,
    setTitle,
    cookingTime,
    setCookingTime,
    ingredients,
    steps,
    mainImagePreview,
    stepImagePreviews,
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
    handleStepVideoUrlChange,
    handleMainImageChange,
    handleStepImageChange,
    handleSubmit,
    handleCancel,
  } = useRecipeEditHandlers(scrollToMessage);

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-6">
        {isEditMode ? t('recipe.edit.title') : t('recipe.create.title')}
      </h1>

      <MessageDisplay ref={messageRef} error={error} />

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
              <Label htmlFor="cookingTime">
                {t('recipe.cookingTime')}（{t('recipe.minutes')}）
              </Label>
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
            {/* メイン画像 */}
            <div>
              <Label>{t('recipe.mainImage')}</Label>
              <ImageUploader
                currentImageUrl={mainImagePreview || undefined}
                onImageSelect={handleMainImageChange}
                onImageRemove={() => handleMainImageChange(null)}
                shape="rectangle"
                height="h-40"
                enableCompression={true}
                compressionOptions={{ maxWidthOrHeight: 1920, maxSizeMB: 1 }}
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
              <div key={index} className="space-y-2">
                {/* PC: 1行横並び、スマホ: 2段組み */}
                {/* 1段目: 食材名（スマホでは幅100%） */}
                <div className="flex flex-col md:flex-row gap-2 md:items-end">
                  <div className="flex-1">
                    <Label>
                      {t('recipe.ingredientName')}
                      <span className="text-red-500 ml-1">*</span>
                    </Label>
                    <Input
                      value={ingredient.name}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                        handleIngredientChange(index, 'name', e.target.value)
                      }
                      required
                    />
                  </div>
                  {/* 2段目（スマホ）/ 同じ行（PC）: 数量・単位・削除ボタン */}
                  <div className="flex gap-2 items-end">
                    <div className="w-20 md:w-24">
                      <Label>{t('recipe.quantity')}</Label>
                      <Input
                        type="number"
                        value={ingredient.quantity ?? ''}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                          handleIngredientChange(
                            index,
                            'quantity',
                            e.target.value ? parseFloat(e.target.value) : undefined
                          )
                        }
                        min={0}
                        step="0.1"
                        placeholder={t('recipe.quantityPlaceholder')}
                      />
                    </div>
                    <div className="flex-1 md:w-32 md:flex-none">
                      <Label>{t('recipe.unit')}</Label>
                      <Input
                        value={ingredient.unit ?? ''}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                          handleIngredientChange(index, 'unit', e.target.value)
                        }
                        placeholder={t('recipe.unitPlaceholder')}
                      />
                    </div>
                    <Button
                      type="button"
                      variant="destructive"
                      size="sm"
                      onClick={() => handleRemoveIngredient(index)}
                      disabled={ingredients.length === 1}
                      className="shrink-0"
                    >
                      {t('common.remove')}
                    </Button>
                  </div>
                </div>
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
              <div key={index} className="border rounded-lg p-4 relative">
                {/* 削除ボタン - 右上に絶対配置、アイコンのみ */}
                <Button
                  type="button"
                  variant="destructive"
                  size="icon"
                  onClick={() => handleRemoveStep(index)}
                  disabled={steps.length === 1}
                  className="absolute top-2 right-2 h-8 w-8"
                  aria-label={t('common.remove')}
                >
                  <Trash2 size={16} />
                </Button>

                {/* 手順番号 - 左上に配置 */}
                <span className="font-bold text-lg mb-2 block">{index + 1}.</span>

                {/* PC: 横並び（テキスト左、画像右）、スマホ: 縦積み */}
                <div className="flex flex-col md:flex-row gap-4 md:gap-4 mt-2">
                  {/* テキスト入力欄 - PC: flex-grow、スマホ: 幅100% */}
                  <div className="flex-1 space-y-2 order-1">
                    <Textarea
                      value={step.description}
                      onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                        handleStepChange(index, e.target.value)
                      }
                      required
                      className="w-full"
                      rows={4}
                    />
                    {/* 動画URL入力 */}
                    <div>
                      <Label className="text-sm">{t('recipe.stepVideoUrl')}</Label>
                      <Input
                        type="url"
                        value={step.videoUrl || ''}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                          handleStepVideoUrlChange(index, e.target.value)
                        }
                        placeholder="https://www.youtube.com/watch?v=..."
                        className="text-sm"
                      />
                    </div>
                  </div>

                  {/* 画像アップロード欄 - PC: 固定サイズ右側、スマホ: 幅100%下段（余白追加） */}
                  <div className="order-2 w-full md:w-[130px] md:flex-shrink-0 md:mt-0">
                    <Label className="text-sm mb-1 block">{t('recipe.stepImage')}</Label>
                    <div className="md:h-[120px]">
                      <ImageUploader
                        currentImageUrl={stepImagePreviews[index] || undefined}
                        onImageSelect={(file) => handleStepImageChange(index, file)}
                        onImageRemove={() => handleStepImageChange(index, null)}
                        shape="rectangle"
                        enableCompression={true}
                        compressionOptions={{ maxWidthOrHeight: 1280, maxSizeMB: 0.5 }}
                      />
                    </div>
                  </div>
                </div>
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
