import { Schedule, CreateScheduleRequest, UpdateScheduleRequest, ScheduleSearchParams } from '../types/schedule';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * スケジュール一覧取得
 */
export const getSchedules = async (
  userId: string,
  params: ScheduleSearchParams
): Promise<Schedule[]> => {
  const response = await fetch(
    `${API_BASE_URL}/api/schedules?startDate=${params.startDate}&endDate=${params.endDate}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
      },
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch schedules');
  }

  return response.json();
};

/**
 * スケジュール作成
 */
export const createSchedule = async (
  userId: string,
  request: CreateScheduleRequest
): Promise<Schedule> => {
  const response = await fetch(`${API_BASE_URL}/api/schedules`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to create schedule');
  }

  return response.json();
};

/**
 * スケジュール更新
 */
export const updateSchedule = async (
  userId: string,
  scheduleId: string,
  request: UpdateScheduleRequest
): Promise<Schedule> => {
  const response = await fetch(`${API_BASE_URL}/api/schedules/${scheduleId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update schedule');
  }

  return response.json();
};

/**
 * スケジュール削除
 */
export const deleteSchedule = async (
  userId: string,
  scheduleId: string
): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/schedules/${scheduleId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to delete schedule');
  }
};

/**
 * 予定を実績に変換
 */
export const convertToCooked = async (
  userId: string,
  scheduleId: string
): Promise<Schedule> => {
  const response = await fetch(
    `${API_BASE_URL}/api/schedules/${scheduleId}/convert-to-cooked`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
      },
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to convert schedule to cooked');
  }

  return response.json();
};
