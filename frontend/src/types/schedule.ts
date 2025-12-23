/**
 * スケジュール型定義
 */

export interface Schedule {
  scheduleId: string;
  userId: string;
  date: string; // YYYY-MM-DD形式
  isDone: boolean;
  recipeId: string;
  recipeTitle: string;
  memo?: string;
  createdAt: string;
}

export interface CreateScheduleRequest {
  date: string; // YYYY-MM-DD形式
  recipeId: string;
  recipeTitle: string;
  memo?: string;
}

export interface UpdateScheduleRequest {
  memo?: string;
}

export interface ScheduleSearchParams {
  startDate: string; // YYYY-MM-DD形式
  endDate: string; // YYYY-MM-DD形式
}

export interface ScheduleState {
  schedules: Schedule[];
  loading: boolean;
  error: string | null;
}
