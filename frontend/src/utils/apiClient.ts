/**
 * API共通ユーティリティ
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface RequestOptions extends RequestInit {
  userId?: string;
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
    ...(fetchOptions.headers as Record<string, string>),
  };

  if (userId) {
    headers['X-User-Id'] = userId;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...fetchOptions,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `Request failed with status ${response.status}`);
  }

  // 204 No Contentの場合はnullを返す
  if (response.status === 204) {
    return null as T;
  }

  return response.json();
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
  const headers: Record<string, string> = {};
  if (userId) {
    headers['X-User-Id'] = userId;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers,
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `Upload failed with status ${response.status}`);
  }

  return response.json();
}

export { API_BASE_URL };
