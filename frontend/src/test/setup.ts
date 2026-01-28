import '@testing-library/jest-dom'
import { vi } from 'vitest'
import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

// Initialize i18n for testing
i18n.use(initReactI18next).init({
  lng: 'ja',
  fallbackLng: 'ja',
  ns: ['translation'],
  defaultNS: 'translation',
  resources: {
    ja: {
      translation: {
        common: {
          login: 'ログイン',
          logout: 'ログアウト',
          register: '新規登録',
          save: '保存',
          cancel: 'キャンセル',
          delete: '削除',
          edit: '編集',
          loading: '読み込み中...',
          user: 'ユーザー',
          noImage: 'No Image',
        },
        nav: {
          recipeSearch: 'レシピ検索',
          schedule: 'スケジュール',
          shoppingList: '買い物リスト',
          inventory: '在庫管理',
          aiChat: 'AIチャット',
          admin: '管理画面',
        },
        header: {
          createRecipe: 'レシピ作成',
          editProfile: 'プロフィール編集',
          openMenu: 'メニューを開く',
        },
        auth: {
          loginTitle: "Let's Cook",
          loginDescription: 'アカウントにログインしてください',
          email: 'メールアドレス',
          password: 'パスワード',
          emailPlaceholder: 'example@email.com',
          passwordPlaceholder: 'パスワードを入力',
          loggingIn: 'ログイン中...',
          noAccount: 'アカウントをお持ちでない方は、新規登録してください',
          errors: {
            emailRequired: 'メールアドレスを入力してください',
            emailInvalid: '有効なメールアドレスを入力してください',
            passwordRequired: 'パスワードを入力してください',
          },
        },
        recipe: {
          list: {
            title: 'レシピ一覧',
            description: 'みんなのレシピを探してみましょう',
            createNew: '新しいレシピを作成',
            allRecipes: 'みんなのレシピ',
            myRecipes: '自分のレシピ',
            noRecipes: 'レシピがありません',
            private: '非公開',
            minutes: '分',
            ingredients: '材料',
            items: '品',
          },
          detail: {
            backToList: 'レシピ一覧に戻る',
            backToListShort: '一覧に戻る',
            ingredients: '材料',
            steps: '作り方',
            stepsCount: 'ステップ',
            deleting: '削除中...',
            confirmDelete: '「{{title}}」を削除してもよろしいですか？',
            notFound: 'レシピが見つかりませんでした',
            createdAt: '作成日',
            updatedAt: '更新日',
            fetchError: 'レシピの取得に失敗しました',
            deleteError: 'レシピの削除に失敗しました',
          },
          form: {
            createTitle: '新しいレシピを作成',
            editTitle: 'レシピを編集',
            createDescription: 'あなたのオリジナルレシピを登録しましょう',
            editDescription: 'レシピの内容を編集できます',
            basicInfo: '基本情報',
            title: 'タイトル',
            titlePlaceholder: '例: 簡単トマトパスタ',
            cookingTime: '調理時間（分）',
            public: '公開する',
            private: '非公開にする',
            recipeImage: 'レシピ画像',
            ingredients: '材料',
            ingredientsDescription: '材料を追加してください（1つ以上必須）',
            ingredientName: '材料名',
            quantity: '分量',
            unit: '単位',
            addIngredient: '材料を追加',
            steps: '作り方',
            stepsDescription: '手順を追加してください（1つ以上必須）',
            stepPlaceholder: 'ステップ {{number}} の説明',
            addStep: '手順を追加',
            saving: '保存中...',
            update: '更新する',
            create: '作成する',
            required: '*',
            fetchError: 'レシピの取得に失敗しました',
            createError: 'レシピの作成に失敗しました',
            updateError: 'レシピの更新に失敗しました',
          },
        },
        language: {
          select: '言語',
          ja: '日本語',
          ko: '한국어',
        },
        zod: {
          required: '必須項目です',
          invalid_type: '入力形式が正しくありません',
          string: {
            min: '{{min}}文字以上で入力してください',
            max: '{{max}}文字以内で入力してください',
            email: '有効なメールアドレスを入力してください',
          },
          number: {
            min: '{{min}}以上の値を入力してください',
            max: '{{max}}以下の値を入力してください',
          },
          array: {
            min: '{{min}}つ以上追加してください',
          },
        },
      },
    },
  },
  interpolation: {
    escapeValue: false,
  },
})

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// Mock ResizeObserver
class ResizeObserverMock {
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()
}

Object.defineProperty(window, 'ResizeObserver', {
  writable: true,
  value: ResizeObserverMock,
})

// Mock URL.createObjectURL / revokeObjectURL
Object.defineProperty(URL, 'createObjectURL', {
  writable: true,
  value: vi.fn(() => 'blob:http://localhost/mock-url'),
})

Object.defineProperty(URL, 'revokeObjectURL', {
  writable: true,
  value: vi.fn(),
})
