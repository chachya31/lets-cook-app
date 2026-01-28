import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { RecipeFormPage } from './RecipeFormPage'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'

// react-router-domのuseNavigateとuseParamsをモック
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => ({}),
  }
})

// recipeApiをモック
const mockCreateRecipe = vi.fn()
const mockUpdateRecipe = vi.fn()
const mockGetRecipe = vi.fn()
const mockUploadImage = vi.fn()

vi.mock('../api/recipeApi', () => ({
  createRecipe: (...args: unknown[]) => mockCreateRecipe(...args),
  updateRecipe: (...args: unknown[]) => mockUpdateRecipe(...args),
  getRecipe: (...args: unknown[]) => mockGetRecipe(...args),
  uploadImage: (...args: unknown[]) => mockUploadImage(...args),
}))

// テスト用ラッパー
const renderWithRouter = (component: React.ReactNode) => {
  return render(<BrowserRouter>{component}</BrowserRouter>)
}

describe('RecipeFormPage', () => {
  beforeEach(() => {
    // 認証状態を設定（ログイン済み）
    useAuthStore.setState({
      user: {
        userId: 'user-123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        profileImageUrl: null,
        preferredLanguage: 'ja',
        timezone: 'Asia/Tokyo',
        roles: [],
      },
      isAuthenticated: true,
    })
    vi.clearAllMocks()
  })

  describe('レンダリング', () => {
    it('タイトル入力欄が表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByLabelText(/タイトル/)).toBeInTheDocument()
    })

    it('調理時間入力欄が表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByLabelText(/調理時間/)).toBeInTheDocument()
    })

    it('材料セクションが表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByText('材料')).toBeInTheDocument()
      expect(screen.getByPlaceholderText('材料名')).toBeInTheDocument()
    })

    it('作り方セクションが表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByText('作り方')).toBeInTheDocument()
      expect(screen.getByPlaceholderText('ステップ 1 の説明')).toBeInTheDocument()
    })

    it('画像アップロードエリアが表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByText('レシピ画像')).toBeInTheDocument()
      expect(screen.getByText('クリックまたはドラッグで画像をアップロード')).toBeInTheDocument()
    })

    it('作成ボタンが表示されること', () => {
      renderWithRouter(<RecipeFormPage />)

      expect(screen.getByRole('button', { name: '作成する' })).toBeInTheDocument()
    })
  })

  describe('動的フォーム', () => {
    it('「材料を追加」ボタンを押すと、材料の入力欄が増えること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // 初期状態で1つの材料入力欄
      expect(screen.getAllByPlaceholderText('材料名')).toHaveLength(1)

      // 「材料を追加」ボタンをクリック
      const addButton = screen.getByRole('button', { name: /材料を追加/ })
      await user.click(addButton)

      // 材料入力欄が2つになる
      expect(screen.getAllByPlaceholderText('材料名')).toHaveLength(2)
    })

    it('「手順を追加」ボタンを押すと、手順の入力欄が増えること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // 初期状態で1つの手順入力欄
      expect(screen.getByPlaceholderText('ステップ 1 の説明')).toBeInTheDocument()

      // 「手順を追加」ボタンをクリック
      const addButton = screen.getByRole('button', { name: /手順を追加/ })
      await user.click(addButton)

      // 手順入力欄が2つになる
      expect(screen.getByPlaceholderText('ステップ 1 の説明')).toBeInTheDocument()
      expect(screen.getByPlaceholderText('ステップ 2 の説明')).toBeInTheDocument()
    })

    it('材料の削除ボタンを押すと、材料の入力欄が減ること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // 材料を2つに増やす
      const addButton = screen.getByRole('button', { name: /材料を追加/ })
      await user.click(addButton)
      expect(screen.getAllByPlaceholderText('材料名')).toHaveLength(2)

      // 削除ボタンをクリック（2番目の材料を削除）
      const deleteButtons = screen.getAllByRole('button').filter(
        (btn) => btn.querySelector('svg.lucide-trash-2')
      )
      await user.click(deleteButtons[1])

      // 材料入力欄が1つに戻る
      expect(screen.getAllByPlaceholderText('材料名')).toHaveLength(1)
    })
  })

  describe('バリデーション', () => {
    it('タイトルを空にして送信しようとするとエラーが表示されること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // タイトルを空のまま送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText('タイトルは必須です')).toBeInTheDocument()
      })
    })

    it('調理時間が0以下の場合にエラーが表示されること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // タイトルを入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      // 調理時間を空に設定（number inputでは0は有効な値として扱われるため、空にする）
      const cookingTimeInput = screen.getByLabelText(/調理時間/)
      await user.clear(cookingTimeInput)

      // 材料と手順を入力
      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText('調理時間は1分以上で入力してください')).toBeInTheDocument()
      })
    })

    it('材料セクションのバリデーションエラーが発生すること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // タイトルを入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      // 材料名を空のまま（デフォルトで空）
      // 手順を入力
      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      // createRecipeが呼ばれないこと（バリデーションでブロック）
      await waitFor(() => {
        expect(mockCreateRecipe).not.toHaveBeenCalled()
      })
    })

    it('手順セクションのバリデーションエラーが発生すること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<RecipeFormPage />)

      // タイトルを入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      // 材料を入力
      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      // 手順を空のまま送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      // createRecipeが呼ばれないこと（バリデーションでブロック）
      await waitFor(() => {
        expect(mockCreateRecipe).not.toHaveBeenCalled()
      })
    })
  })

  describe('送信', () => {
    it('必須項目を入力して送信ボタンを押すと、createRecipe APIが呼ばれること', async () => {
      const user = userEvent.setup()
      mockCreateRecipe.mockResolvedValue({ recipeId: 'new-recipe-123' })

      renderWithRouter(<RecipeFormPage />)

      // タイトルを入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      // 材料を入力
      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      // 手順を入力
      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockCreateRecipe).toHaveBeenCalledTimes(1)
        expect(mockCreateRecipe).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'テストレシピ',
            ingredients: expect.arrayContaining([
              expect.objectContaining({ name: 'トマト' }),
            ]),
            steps: expect.arrayContaining([
              expect.objectContaining({ description: 'トマトを切る' }),
            ]),
          })
        )
      })
    })

    it('送信成功後にレシピ詳細ページへナビゲートすること', async () => {
      const user = userEvent.setup()
      mockCreateRecipe.mockResolvedValue({ recipeId: 'new-recipe-123' })

      renderWithRouter(<RecipeFormPage />)

      // 必須項目を入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/recipes/new-recipe-123')
      })
    })

    it('送信中はボタンが無効化され「保存中...」と表示されること', async () => {
      const user = userEvent.setup()
      // APIが解決しないようにする
      mockCreateRecipe.mockImplementation(() => new Promise(() => {}))

      renderWithRouter(<RecipeFormPage />)

      // 必須項目を入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: '保存中...' })).toBeDisabled()
      })
    })

    it('API エラー時にエラーメッセージが表示されること', async () => {
      const user = userEvent.setup()
      mockCreateRecipe.mockRejectedValue(new Error('API Error'))

      renderWithRouter(<RecipeFormPage />)

      // 必須項目を入力
      const titleInput = screen.getByLabelText(/タイトル/)
      await user.type(titleInput, 'テストレシピ')

      const ingredientInput = screen.getByPlaceholderText('材料名')
      await user.type(ingredientInput, 'トマト')

      const stepInput = screen.getByPlaceholderText('ステップ 1 の説明')
      await user.type(stepInput, 'トマトを切る')

      // 送信
      const submitButton = screen.getByRole('button', { name: '作成する' })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText('レシピの作成に失敗しました')).toBeInTheDocument()
      })
    })
  })

  describe('認証チェック', () => {
    it('未認証の場合はログインページへリダイレクトされること', async () => {
      useAuthStore.setState({
        user: null,
        isAuthenticated: false,
      })

      renderWithRouter(<RecipeFormPage />)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login')
      })
    })
  })
})
