/**
 * API共通ユーティリティ
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

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

  if (userId) {
    headers['X-User-Id'] = userId;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...fetchOptions,
      headers,
    });

    if (!response.ok) {
      const errorData: ErrorResponse = await response.json().catch(() => ({
        code: 'UNKNOWN_ERROR',
        message: 'Unknown error occurred',
        timestamp: new Date().toISOString(),
      }));

      // ステータスコードに応じたエラーメッセージ
      let errorMessage = errorData.message;
      
      switch (response.status) {
        case 400:
          errorMessage = errorData.message || 'Invalid request';
          break;
        case 401:
          errorMessage = errorData.message || 'Authentication required';
          break;
        case 403:
          errorMessage = errorData.message || 'Access forbidden';
          break;
        case 404:
          errorMessage = errorData.message || 'Resource not found';
          break;
        case 409:
          errorMessage = errorData.message || 'Resource conflict';
          break;
        case 500:
          errorMessage = errorData.message || 'Server error occurred';
          break;
        default:
          errorMessage = errorData.message || `Request failed with status ${response.status}`;
      }

      throw new Error(errorMessage);
    }

    // 204 No Contentの場合はnullを返す
    if (response.status === 204) {
      return null as T;
    }

    return response.json();
  } catch (error) {
    // ネットワークエラーの場合
    if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
      throw new Error('Network error: Please check your internet connection');
    }
    
    // その他のエラーはそのまま投げる
    throw error;
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
export async function apiPost<T>(
  endpoint: string,
  data: unknown,
  userId?: string
): Promise<T> {
  return fetchWithErrorHandling<T>(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
    userId,
  });
}

/**
 * PUT リクエスト
 */
export async function apiPut<T>(
  endpoint: string,
  data: unknown,
  userId?: string
): Promise<T> {
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
  
  if (userId) {
    headers['X-User-Id'] = userId;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      const errorData: ErrorResponse = await response.json().catch(() => ({
        code: 'UPLOAD_ERROR',
        message: 'Upload failed',
        timestamp: new Date().toISOString(),
      }));

      throw new Error(errorData.message || `Upload failed with status ${response.status}`);
    }

    return response.json();
  } catch (error) {
    // ネットワークエラーの場合
    if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
      throw new Error('Network error: Please check your internet connection');
    }
    
    // その他のエラーはそのまま投げる
    throw error;
  }
}

export { API_BASE_URL };
