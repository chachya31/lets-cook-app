import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createRecipeWithImages,
  updateRecipe as updateRecipeApi,
  uploadRecipeImage,
  uploadStepImage,
} from '../../../api/recipeApi';
import { useAuthStore } from '../../../store/authStore';
import { clearCurrentRecipe, fetchRecipe } from '../../../store/recipeSlice';
import { AppDispatch, RootState } from '../../../store/store';
import { Ingredient, Step } from '../../../types/recipe';
import { initialFormState } from './recipeEditConfig';

/**
 * レシピ編集のイベントハンドラーとロジックを管理するカスタムフック
 * @param scrollToMessage エラー時にスクロールするコールバック
 */
export const useRecipeEditHandlers = (scrollToMessage?: () => void) => {
  const { id } = useParams<{ id: string }>();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const {
    currentRecipe,
    loading: recipeLoading,
    error,
  } = useSelector((state: RootState) => state.recipe);
  const user = useAuthStore((state) => state.user);

  const [title, setTitle] = useState(initialFormState.title);
  const [cookingTime, setCookingTime] = useState(initialFormState.cookingTime);
  const [ingredients, setIngredients] = useState<Ingredient[]>(initialFormState.ingredients);
  const [steps, setSteps] = useState<Step[]>(initialFormState.steps);
  const [mainImage, setMainImage] = useState<File | null>(null);
  const [mainImagePreview, setMainImagePreview] = useState<string | null>(null);
  const [stepImages, setStepImages] = useState<(File | null)[]>([]);
  const [stepImagePreviews, setStepImagePreviews] = useState<(string | null)[]>([]);
  const [loading, setLoading] = useState(false);

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
      setMainImagePreview(currentRecipe.imageUrl || null);
      setStepImages(new Array(currentRecipe.steps.length).fill(null));
      setStepImagePreviews(currentRecipe.steps.map((s) => s.imageUrl || null));
    }
  }, [currentRecipe, isEditMode]);

  // 食材の追加
  const handleAddIngredient = () => {
    setIngredients([
      ...ingredients,
      { name: '', quantity: undefined, unit: '', note: '', optional: false },
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
    value: string | number | boolean | undefined
  ) => {
    const newIngredients = [...ingredients];
    newIngredients[index] = { ...newIngredients[index], [field]: value };
    setIngredients(newIngredients);
  };

  // 手順の追加
  const handleAddStep = () => {
    setSteps([...steps, { description: '', imageUrl: undefined, videoUrl: undefined }]);
    setStepImages([...stepImages, null]);
    setStepImagePreviews([...stepImagePreviews, null]);
  };

  // 手順の削除
  const handleRemoveStep = (index: number) => {
    setSteps(steps.filter((_, i) => i !== index));
    setStepImages(stepImages.filter((_, i) => i !== index));
    setStepImagePreviews(stepImagePreviews.filter((_, i) => i !== index));
  };

  // 手順の変更
  const handleStepChange = (index: number, value: string) => {
    const newSteps = [...steps];
    newSteps[index] = { ...newSteps[index], description: value };
    setSteps(newSteps);
  };

  // 手順の動画URL変更
  const handleStepVideoUrlChange = (index: number, value: string) => {
    const newSteps = [...steps];
    newSteps[index] = { ...newSteps[index], videoUrl: value || undefined };
    setSteps(newSteps);
  };

  // メイン画像の変更
  const handleMainImageChange = (file: File | null) => {
    setMainImage(file);
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setMainImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    } else {
      setMainImagePreview(null);
    }
  };

  // 手順画像の変更
  const handleStepImageChange = (index: number, file: File | null) => {
    const newStepImages = [...stepImages];
    newStepImages[index] = file;
    setStepImages(newStepImages);

    const newPreviews = [...stepImagePreviews];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        newPreviews[index] = reader.result as string;
        setStepImagePreviews([...newPreviews]);
      };
      reader.readAsDataURL(file);
    } else {
      newPreviews[index] = null;
      setStepImagePreviews(newPreviews);
    }
  };

  // フォーム送信
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setLoading(true);
    try {
      const filteredSteps = steps.filter((s) => s.description.trim() !== '');

      if (isEditMode && id) {
        // 編集モード: 既存APIで更新後、画像があればアップロード
        const recipeData = {
          title,
          ingredients,
          steps: filteredSteps,
          cookingTime,
        };
        await updateRecipeApi(id, user.userId, recipeData);

        // メイン画像がある場合はアップロード
        if (mainImage) {
          await uploadRecipeImage(id, user.userId, mainImage);
        }

        // 手順画像がある場合はアップロード
        for (let i = 0; i < stepImages.length; i++) {
          const stepImage = stepImages[i];
          if (stepImage) {
            await uploadStepImage(id, i, user.userId, stepImage);
          }
        }

        navigate(`/recipes/${id}`);
      } else {
        // 新規作成: 画像付きAPIを使用
        const recipe = await createRecipeWithImages(
          user.userId,
          title,
          ingredients,
          filteredSteps,
          cookingTime,
          mainImage || undefined,
          stepImages.filter((f): f is File => f !== null).length > 0 ? stepImages : undefined
        );
        navigate(`/recipes/${recipe.recipeId}`);
      }
    } catch (err) {
      if (scrollToMessage) scrollToMessage();
    } finally {
      setLoading(false);
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
    mainImage,
    mainImagePreview,
    stepImages,
    stepImagePreviews,
    isEditMode,
    loading: loading || recipeLoading,
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
  };
};
