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
  fetchSchedules: (userId: string, params: ScheduleSearchParams) => Promise<void>;
  createSchedule: (userId: string, request: CreateScheduleRequest) => Promise<void>;
  updateSchedule: (
    userId: string,
    scheduleId: string,
    request: UpdateScheduleRequest
  ) => Promise<void>;
  deleteSchedule: (userId: string, scheduleId: string) => Promise<void>;
  markAsDone: (userId: string, scheduleId: string) => Promise<void>;
  clearError: () => void;
}

type ScheduleStore = ScheduleState & ScheduleActions;

export const useScheduleStore = create<ScheduleStore>((set) => ({
  // 初期状態
  schedules: [],
  loading: false,
  error: null,

  // スケジュール一覧取得
  fetchSchedules: async (userId: string, params: ScheduleSearchParams) => {
    set({ loading: true, error: null });
    try {
      const schedules = await scheduleApi.getSchedules(userId, params);
      set({ schedules, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to fetch schedules',
      });
    }
  },

  // スケジュール作成
  createSchedule: async (userId: string, request: CreateScheduleRequest) => {
    set({ loading: true, error: null });
    try {
      const schedule = await scheduleApi.createSchedule(userId, request);
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
  updateSchedule: async (userId: string, scheduleId: string, request: UpdateScheduleRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedSchedule = await scheduleApi.updateSchedule(userId, scheduleId, request);
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
  deleteSchedule: async (userId: string, scheduleId: string) => {
    set({ loading: true, error: null });
    try {
      await scheduleApi.deleteSchedule(userId, scheduleId);
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
  markAsDone: async (userId: string, scheduleId: string) => {
    set({ loading: true, error: null });
    try {
      const updatedSchedule = await scheduleApi.markAsDone(userId, scheduleId);
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
