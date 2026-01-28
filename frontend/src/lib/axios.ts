import axios from 'axios'

const AUTH_STORAGE_KEY = 'auth-storage'

export const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Request interceptor - トークンを自動付与
apiClient.interceptors.request.use(
  (config) => {
    try {
      const stored = localStorage.getItem(AUTH_STORAGE_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        const accessToken = parsed?.state?.accessToken
        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`
        }
      }
    } catch {
      // localStorageのパースエラーは無視
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - 401エラー時にログアウト
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // 認証情報をクリア
      localStorage.removeItem(AUTH_STORAGE_KEY)
      // ログインページへリダイレクト（現在のページがログインページでない場合）
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// ファイルアップロード用のPOST関数（multipart/form-data）
export const apiPostMultipart = async <T>(url: string, formData: FormData): Promise<T> => {
  const response = await apiClient.post<T>(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return response.data
}
