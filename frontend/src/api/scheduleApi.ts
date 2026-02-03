import {
  CreateScheduleRequest,
  Schedule,
  ScheduleSearchParams,
  UpdateScheduleRequest,
} from '../types/schedule';
import { apiDelete, apiGet, apiPost, apiPut } from '../utils/apiClient';

/**
 * スケジュール一覧取得
 */
export const getSchedules = async (params: ScheduleSearchParams): Promise<Schedule[]> => {
  return apiGet<Schedule[]>(
    `/api/schedules?startDate=${params.startDate}&endDate=${params.endDate}`
  );
};

/**
 * スケジュール作成
 */
export const createSchedule = async (request: CreateScheduleRequest): Promise<Schedule> => {
  return apiPost<Schedule>('/api/schedules', request);
};

/**
 * スケジュール更新
 */
export const updateSchedule = async (
  scheduleId: string,
  request: UpdateScheduleRequest
): Promise<Schedule> => {
  return apiPut<Schedule>(`/api/schedules/${scheduleId}`, request);
};

/**
 * スケジュール削除
 */
export const deleteSchedule = async (scheduleId: string): Promise<void> => {
  return apiDelete<void>(`/api/schedules/${scheduleId}`);
};

/**
 * 予定を実績に変換（完了にする）
 */
export const markAsDone = async (scheduleId: string): Promise<Schedule> => {
  return apiPost<Schedule>(`/api/schedules/${scheduleId}/mark-done`, {});
};
