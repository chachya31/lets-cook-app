import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { AppDispatch, RootState } from '../../store/store';
import { fetchRecipe, createRecipe, updateRecipe, clearCurrentRecipe } from '../../store/recipeSlice';
import { useTranslation } from 'react-i18next';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Ingredient } from '../../types/recipe';

/**
 * レシピ編集ページ
 */
const RecipeEditPage: React.FC = () => {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { currentRecipe, loading, error } = useSelector((state: RootState) => state.recipe);
  const { user } = useSelector((state: RootState) => state.auth);

  const [title, setTitle] = useState('');
  const [cookingTime, setCookingTime] = useState(0);
  const [ingredients, setIngredients] = useState<Ingredient[]>([
    { name: '', quantity: 0, unit: 'g', note: '', optional: false },
  ]);
  const [steps, setSteps] = useState<string[]>(['']);

  const isEditMode = !!id;

  useEffect(() => {
    if (isEditMode && id) {
      dispatch(fetchRecipe(id));
    }
    return () => {
      dispatch(clearCurrentRecipe());
    };
  }, [dispatch, id, isEditMode]);

  useEffect(() => {
    if (currentRecipe && isEditMode) {
      setTitle(currentRecipe.title);
      setCookingTime(currentRecipe.cookingTime);
      setIngredients(currentRecipe.ingredients);
      setSteps(currentRecipe.steps);
    }
  }, [currentRecipe, isEditMode]);

  const handleAddIngredient = () => {
    setIngredients([...ingredients, { name: '', quantity: 0, unit: 'g', note: '', optional: false }]);
  };

  const handleRemoveIngredient = (index: number) => {
    setIngredients(ingredients.filter((_, i) => i !== index));
  };

  const handleIngredientChange = (index: number, field: keyof Ingredient, value: string | number | boolean) => {
    const newIngredients = [...ingredients];
    newIngredients[index] = { ...newIngredients[index], [field]: value };
    setIngredients(newIngredients);
  };

  const handleAddStep = () => {
    setSteps([...steps, '']);
  };

  const handleRemoveStep = (index: number) => {
    setSteps(steps.filter((_, i) => i !== index));
  };

  const handleStepChange = (index: number, value: string) => {
    const newSteps = [...steps];
    newSteps[index] = value;
    setSteps(newSteps);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    const recipeData = {
      title,
      ingredients,
      steps: steps.filter((s) => s.trim() !== ''),
      cookingTime,
    };

    if (isEditMode && id) {
      await dispatch(updateRecipe({ recipeId: id, userId: user.userId, recipe: recipeData }));
      navigate(`/recipes/${id}`);
    } else {
      const result = await dispatch(createRecipe({ userId: user.userId, recipe: recipeData }));
      if (result.meta.requestStatus === 'fulfilled') {
        const newRecipe = result.payload as any;
        navigate(`/recipes/${newRecipe.recipeId}`);
      }
    }
  };

  const handleCancel = () => {
    if (isEditMode && id) {
      navigate(`/recipes/${id}`);
    } else {
      navigate('/recipes');
    }
  };

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
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCookingTime(parseInt(e.target.value) || 0)}
                required
                min={0}
              />
            </div>
          </CardContent>
        </Card>

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
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleIngredientChange(index, 'name', e.target.value)}
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
                <div className="w-24">
                  <Label>{t('recipe.unit')}</Label>
                  <Input
                    value={ingredient.unit}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleIngredientChange(index, 'unit', e.target.value)}
                    required
                  />
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
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => handleStepChange(index, e.target.value)}
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
