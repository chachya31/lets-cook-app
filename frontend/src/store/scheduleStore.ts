/**
 * Schedule Store (Zustand)
 * スケジュール状態管理
 */

import { create } from 'zustand';
import * as scheduleApi from '../api/scheduleApi';
import {
  CreateScheduleRequest,
  Schedule,
  ScheduleSearchParams,
  UpdateScheduleRequest,
} from '../types/schedule';

interface ScheduleState {
  schedules: Schedule[];
  loading: boolean;
  error: string | null;
}

interface ScheduleActions {
  fetchSchedules: (params: ScheduleSearchParams) => Promise<void>;
  createSchedule: (request: CreateScheduleRequest) => Promise<void>;
  updateSchedule: (scheduleId: string, request: UpdateScheduleRequest) => Promise<void>;
  deleteSchedule: (scheduleId: string) => Promise<void>;
  markAsDone: (scheduleId: string) => Promise<void>;
  clearError: () => void;
}

type ScheduleStore = ScheduleState & ScheduleActions;

export const useScheduleStore = create<ScheduleStore>((set) => ({
  // 初期状態
  schedules: [],
  loading: false,
  error: null,

  // スケジュール一覧取得
  fetchSchedules: async (params: ScheduleSearchParams) => {
    set({ loading: true, error: null });
    try {
      const schedules = await scheduleApi.getSchedules(params);
      set({ schedules, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to fetch schedules',
      });
    }
  },

  // スケジュール作成
  createSchedule: async (request: CreateScheduleRequest) => {
    set({ loading: true, error: null });
    try {
      const schedule = await scheduleApi.createSchedule(request);
      set((state) => ({
        schedules: [...state.schedules, schedule],
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to create schedule',
      });
      throw error;
    }
  },

  // スケジュール更新
  updateSchedule: async (scheduleId: string, request: UpdateScheduleRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedSchedule = await scheduleApi.updateSchedule(scheduleId, request);
      set((state) => ({
        schedules: state.schedules.map((s) => (s.scheduleId === scheduleId ? updatedSchedule : s)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to update schedule',
      });
      throw error;
    }
  },

  // スケジュール削除
  deleteSchedule: async (scheduleId: string) => {
    set({ loading: true, error: null });
    try {
      await scheduleApi.deleteSchedule(scheduleId);
      set((state) => ({
        schedules: state.schedules.filter((s) => s.scheduleId !== scheduleId),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to delete schedule',
      });
      throw error;
    }
  },

  // 予定を実績に変換（完了にする）
  markAsDone: async (scheduleId: string) => {
    set({ loading: true, error: null });
    try {
      const updatedSchedule = await scheduleApi.markAsDone(scheduleId);
      set((state) => ({
        schedules: state.schedules.map((s) => (s.scheduleId === scheduleId ? updatedSchedule : s)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to mark schedule as done',
      });
      throw error;
    }
  },

  // エラーをクリア
  clearError: () => {
    set({ error: null });
  },
}));
