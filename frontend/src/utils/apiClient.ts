/**
 * API共通ユーティリティ
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * セッション期限切れフラグ（リダイレクト重複防止用）
 */
let isSessionExpiredRedirecting = false;

/**
 * セッション期限切れ時の処理
 * 注意: 循環参照を避けるため、storeを直接importせずlocalStorageのみクリア
 * ページリダイレクト後にアプリが再読み込みされ、Redux状態も初期化される
 */
const handleSessionExpired = (): void => {
  if (isSessionExpiredRedirecting) {
    return;
  }
  isSessionExpiredRedirecting = true;

  // localStorageから認証情報をクリア
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('idToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('user');
  localStorage.removeItem('alert_dismissed_date');

  // セッション期限切れフラグをlocalStorageに保存（ログインページで表示用）
  localStorage.setItem('sessionExpired', 'true');

  // ログインページにリダイレクト
  window.location.href = '/login';
};

interface RequestOptions extends RequestInit {
  userId?: string;
}

/**
 * エラーレスポンスの型定義
 */
interface ErrorResponse {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp: string;
}

/**
 * デフォルトのエラーレスポンスを生成
 */
const createDefaultErrorResponse = (code: string, message: string): ErrorResponse => ({
  code,
  message,
  timestamp: new Date().toISOString(),
});

/**
 * ステータスコードに応じたデフォルトメッセージを取得
 */
const getDefaultErrorMessage = (status: number): string => {
  const messages: Record<number, string> = {
    400: 'Invalid request',
    401: 'Authentication required',
    403: 'Access forbidden',
    404: 'Resource not found',
    409: 'Resource conflict',
    500: 'Server error occurred',
  };
  return messages[status] || `Request failed with status ${status}`;
};

/**
 * ネットワークエラーをハンドリング
 */
const handleNetworkError = (error: unknown): never => {
  if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
    throw new Error('Network error: Please check your internet connection');
  }
  throw error;
};

/**
 * レスポンスエラーをハンドリング
 */
const handleResponseError = async (response: Response): Promise<never> => {
  // 401エラーの場合はセッション期限切れとして処理
  if (response.status === 401) {
    handleSessionExpired();
    throw new Error('Session expired');
  }

  const errorData: ErrorResponse = await response
    .json()
    .catch(() => createDefaultErrorResponse('UNKNOWN_ERROR', 'Unknown error occurred'));
  const errorMessage = errorData.message || getDefaultErrorMessage(response.status);
  throw new Error(errorMessage);
};

/**
 * 共通のfetchラッパー
 */
async function fetchWithErrorHandling<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { userId, ...fetchOptions } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Accept-Language': localStorage.getItem('i18nextLng') || 'ja',
    ...(fetchOptions.headers as Record<string, string>),
  };

  // JWTトークンをAuthorizationヘッダーに追加
  const token = localStorage.getItem('accessToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // X-User-Idヘッダーを追加（明示的に指定されていない場合はlocalStorageから取得）
  const effectiveUserId = userId || localStorage.getItem('userId');
  if (effectiveUserId) {
    headers['X-User-Id'] = effectiveUserId;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...fetchOptions,
      headers,
    });

    if (!response.ok) {
      await handleResponseError(response);
    }

    // 204 No Contentまたはボディが空の場合はnullを返す
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null as T;
    }

    // Content-Typeをチェック
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      const text = await response.text();
      return text ? JSON.parse(text) : (null as T);
    }

    return null as T;
  } catch (error) {
    return handleNetworkError(error);
  }
}

/**
 * GET リクエスト
 */
export async function apiGet<T>(endpoint: string, userId?: string): Promise<T> {
  return fetchWithErrorHandling<T>(endpoint, {
    method: 'GET',
    userId,
  });
}

/**
 * POST リクエスト
 */
export async function apiPost<T>(endpoint: string, data: unknown, userId?: string): Promise<T> {
  return fetchWithErrorHandling<T>(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
    userId,
  });
}

/**
 * PUT リクエスト
 */
export async function apiPut<T>(endpoint: string, data: unknown, userId?: string): Promise<T> {
  return fetchWithErrorHandling<T>(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data),
    userId,
  });
}

/**
 * DELETE リクエスト
 */
export async function apiDelete<T>(endpoint: string, userId?: string): Promise<T> {
  return fetchWithErrorHandling<T>(endpoint, {
    method: 'DELETE',
    userId,
  });
}

/**
 * ファイルアップロード用POST
 */
export async function apiPostFile<T>(
  endpoint: string,
  formData: FormData,
  userId?: string
): Promise<T> {
  const headers: Record<string, string> = {
    'Accept-Language': localStorage.getItem('i18nextLng') || 'ja',
  };

  // JWTトークンをAuthorizationヘッダーに追加
  const token = localStorage.getItem('accessToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // X-User-Idヘッダーを追加（明示的に指定されていない場合はlocalStorageから取得）
  const effectiveUserId = userId || localStorage.getItem('userId');
  if (effectiveUserId) {
    headers['X-User-Id'] = effectiveUserId;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      await handleResponseError(response);
    }

    return response.json();
  } catch (error) {
    return handleNetworkError(error);
  }
}

export { API_BASE_URL };
