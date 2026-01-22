import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { LoginPage } from './LoginPage'
import { useAuthStore } from '../stores/useAuthStore'

// react-router-domのuseNavigateをモック
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

// テスト用ラッパー
const renderWithRouter = (component: React.ReactNode) => {
  return render(<BrowserRouter>{component}</BrowserRouter>)
}

describe('LoginPage', () => {
  beforeEach(() => {
    // 各テスト前にストアをリセット
    useAuthStore.setState({
      user: null,
      accessToken: null,
      idToken: null,
      refreshToken: null,
      expiresIn: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    })
    vi.clearAllMocks()
  })

  describe('フォーム表示', () => {
    it('メールアドレス入力フォームが表示されること', () => {
      renderWithRouter(<LoginPage />)

      const emailInput = screen.getByLabelText('メールアドレス')
      expect(emailInput).toBeInTheDocument()
      expect(emailInput).toHaveAttribute('type', 'email')
    })

    it('パスワード入力フォームが表示されること', () => {
      renderWithRouter(<LoginPage />)

      const passwordInput = screen.getByLabelText('パスワード')
      expect(passwordInput).toBeInTheDocument()
      expect(passwordInput).toHaveAttribute('type', 'password')
    })

    it('ログインボタンが表示されること', () => {
      renderWithRouter(<LoginPage />)

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      expect(loginButton).toBeInTheDocument()
    })
  })

  describe('バリデーション', () => {
    it('メールアドレスが空の場合にエラーメッセージが表示されること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<LoginPage />)

      const passwordInput = screen.getByLabelText('パスワード')
      await user.type(passwordInput, 'password123')

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      expect(screen.getByText('メールアドレスを入力してください')).toBeInTheDocument()
    })

    it('メールアドレスが無効な形式の場合にエラーメッセージが表示されること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<LoginPage />)

      // HTML5バリデーションは通過するが、カスタム正規表現（@xxx.xxx形式を要求）で失敗するメールアドレス
      // カスタムパターン: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const emailInput = screen.getByLabelText('メールアドレス')
      await user.type(emailInput, 'test@invalid')

      const passwordInput = screen.getByLabelText('パスワード')
      await user.type(passwordInput, 'password123')

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      await waitFor(() => {
        expect(screen.getByText('有効なメールアドレスを入力してください')).toBeInTheDocument()
      })
    })

    it('パスワードが空の場合にエラーメッセージが表示されること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<LoginPage />)

      const emailInput = screen.getByLabelText('メールアドレス')
      await user.type(emailInput, 'test@example.com')

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      expect(screen.getByText('パスワードを入力してください')).toBeInTheDocument()
    })

    it('入力エラーは入力時にクリアされること', async () => {
      const user = userEvent.setup()
      renderWithRouter(<LoginPage />)

      // バリデーションエラーを発生させる
      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      expect(screen.getByText('メールアドレスを入力してください')).toBeInTheDocument()

      // 入力するとエラーがクリアされる
      const emailInput = screen.getByLabelText('メールアドレス')
      await user.type(emailInput, 't')

      expect(screen.queryByText('メールアドレスを入力してください')).not.toBeInTheDocument()
    })
  })

  describe('ログイン処理', () => {
    it('ログインボタン押下時にloginアクションが呼ばれること', async () => {
      const user = userEvent.setup()
      const mockLogin = vi.fn().mockResolvedValue(undefined)
      useAuthStore.setState({ login: mockLogin })

      renderWithRouter(<LoginPage />)

      const emailInput = screen.getByLabelText('メールアドレス')
      await user.type(emailInput, 'test@example.com')

      const passwordInput = screen.getByLabelText('パスワード')
      await user.type(passwordInput, 'password123')

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledTimes(1)
        expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123')
      })
    })

    it('ログイン中はボタンが無効化されローディング表示になること', async () => {
      // isLoadingがtrueの状態でレンダリング
      useAuthStore.setState({ isLoading: true })

      renderWithRouter(<LoginPage />)

      const loginButton = screen.getByRole('button', { name: /ログイン中/ })
      expect(loginButton).toBeInTheDocument()
      expect(loginButton).toBeDisabled()
    })
  })

  describe('エラー表示', () => {
    it('エラー発生時にアラートが表示されること', () => {
      const errorMessage = '認証に失敗しました'
      // clearErrorをモックして、マウント時にエラーがクリアされないようにする
      const mockClearError = vi.fn()
      useAuthStore.setState({ error: errorMessage, clearError: mockClearError })

      renderWithRouter(<LoginPage />)

      expect(screen.getByText(errorMessage)).toBeInTheDocument()
    })

    it('エラーがない場合はアラートが表示されないこと', () => {
      useAuthStore.setState({ error: null })

      renderWithRouter(<LoginPage />)

      // AlertDescriptionコンポーネントが存在しないことを確認
      expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    })
  })

  describe('リダイレクト', () => {
    it('既にログイン済みの場合はトップページへリダイレクトされること', async () => {
      useAuthStore.setState({ isAuthenticated: true })

      renderWithRouter(<LoginPage />)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/')
      })
    })

    it('ログイン成功時にトップページへリダイレクトされること', async () => {
      const user = userEvent.setup()
      const mockLogin = vi.fn().mockResolvedValue(undefined)
      useAuthStore.setState({ login: mockLogin })

      renderWithRouter(<LoginPage />)

      const emailInput = screen.getByLabelText('メールアドレス')
      await user.type(emailInput, 'test@example.com')

      const passwordInput = screen.getByLabelText('パスワード')
      await user.type(passwordInput, 'password123')

      const loginButton = screen.getByRole('button', { name: 'ログイン' })
      await user.click(loginButton)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/')
      })
    })
  })
})
