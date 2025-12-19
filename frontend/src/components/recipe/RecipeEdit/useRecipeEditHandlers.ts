import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import {
  clearCurrentRecipe,
  createRecipe,
  fetchRecipe,
  updateRecipe,
} from '../../../store/recipeSlice';
import { AppDispatch, RootState } from '../../../store/store';
import { Ingredient } from '../../../types/recipe';
import { initialFormState } from './recipeEditConfig';

/**
 * レシピ編集のイベントハンドラーとロジックを管理するカスタムフック
 * @param scrollToMessage エラー時にスクロールするコールバック
 */
export const useRecipeEditHandlers = (scrollToMessage?: () => void) => {
  const { id } = useParams<{ id: string }>();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { currentRecipe, loading, error } = useSelector((state: RootState) => state.recipe);
  const { user } = useSelector((state: RootState) => state.auth);

  const [title, setTitle] = useState(initialFormState.title);
  const [cookingTime, setCookingTime] = useState(initialFormState.cookingTime);
  const [ingredients, setIngredients] = useState<Ingredient[]>(initialFormState.ingredients);
  const [steps, setSteps] = useState<string[]>(initialFormState.steps);

  const isEditMode = !!id;

  // レシピデータの取得
  useEffect(() => {
    if (isEditMode && id) {
      dispatch(fetchRecipe(id));
    }
    return () => {
      dispatch(clearCurrentRecipe());
    };
  }, [dispatch, id, isEditMode]);

  // エラー発生時にスクロール
  useEffect(() => {
    if (error && scrollToMessage) {
      scrollToMessage();
    }
  }, [error, scrollToMessage]);

  // 編集モード時のフォーム初期化
  useEffect(() => {
    if (currentRecipe && isEditMode) {
      setTitle(currentRecipe.title);
      setCookingTime(currentRecipe.cookingTime);
      setIngredients(currentRecipe.ingredients);
      setSteps(currentRecipe.steps);
    }
  }, [currentRecipe, isEditMode]);

  // 食材の追加
  const handleAddIngredient = () => {
    setIngredients([
      ...ingredients,
      { name: '', quantity: 0, unit: 'g', note: '', optional: false },
    ]);
  };

  // 食材の削除
  const handleRemoveIngredient = (index: number) => {
    setIngredients(ingredients.filter((_, i) => i !== index));
  };

  // 食材の変更
  const handleIngredientChange = (
    index: number,
    field: keyof Ingredient,
    value: string | number | boolean
  ) => {
    const newIngredients = [...ingredients];
    newIngredients[index] = { ...newIngredients[index], [field]: value };
    setIngredients(newIngredients);
  };

  // 手順の追加
  const handleAddStep = () => {
    setSteps([...steps, '']);
  };

  // 手順の削除
  const handleRemoveStep = (index: number) => {
    setSteps(steps.filter((_, i) => i !== index));
  };

  // 手順の変更
  const handleStepChange = (index: number, value: string) => {
    const newSteps = [...steps];
    newSteps[index] = value;
    setSteps(newSteps);
  };

  // フォーム送信
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

  // キャンセル
  const handleCancel = () => {
    if (isEditMode && id) {
      navigate(`/recipes/${id}`);
    } else {
      navigate('/recipes');
    }
  };

  return {
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
  };
};
