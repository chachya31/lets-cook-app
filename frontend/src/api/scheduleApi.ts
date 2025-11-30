import { Schedule, CreateScheduleRequest, UpdateScheduleRequest, ScheduleSearchParams } from '../types/schedule';
import { apiGet, apiPost, apiPut, apiDelete } from '../utils/apiClient';

/**
 * スケジュール一覧取得
 */
export const getSchedules = async (
  userId: string,
  params: ScheduleSearchParams
): Promise<Schedule[]> => {
  return apiGet<Schedule[]>(
    `/api/schedules?startDate=${params.startDate}&endDate=${params.endDate}`,
    userId
  );
};

/**
 * スケジュール作成
 */
export const createSchedule = async (
  userId: string,
  request: CreateScheduleRequest
): Promise<Schedule> => {
  return apiPost<Schedule>('/api/schedules', request, userId);
};

/**
 * スケジュール更新
 */
export const updateSchedule = async (
  userId: string,
  scheduleId: string,
  request: UpdateScheduleRequest
): Promise<Schedule> => {
  return apiPut<Schedule>(`/api/schedules/${scheduleId}`, request, userId);
};

/**
 * スケジュール削除
 */
export const deleteSchedule = async (
  userId: string,
  scheduleId: string
): Promise<void> => {
  return apiDelete<void>(`/api/schedules/${scheduleId}`, userId);
};

/**
 * 予定を実績に変換
 */
export const convertToCooked = async (
  userId: string,
  scheduleId: string
): Promise<Schedule> => {
  return apiPost<Schedule>(`/api/schedules/${scheduleId}/convert-to-cooked`, {}, userId);
};
