import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import * as scheduleApi from '../../api/scheduleApi';
import {
  CreateScheduleRequest,
  Schedule,
  ScheduleSearchParams,
  ScheduleState,
  UpdateScheduleRequest,
} from '../../types/schedule';

const initialState: ScheduleState = {
  schedules: [],
  loading: false,
  error: null,
};

/**
 * スケジュール一覧取得
 */
export const fetchSchedules = createAsyncThunk(
  'schedule/fetchSchedules',
  async ({ userId, params }: { userId: string; params: ScheduleSearchParams }) => {
    return await scheduleApi.getSchedules(userId, params);
  }
);

/**
 * スケジュール作成
 */
export const createSchedule = createAsyncThunk(
  'schedule/createSchedule',
  async ({ userId, request }: { userId: string; request: CreateScheduleRequest }) => {
    return await scheduleApi.createSchedule(userId, request);
  }
);

/**
 * スケジュール更新
 */
export const updateSchedule = createAsyncThunk(
  'schedule/updateSchedule',
  async ({
    userId,
    scheduleId,
    request,
  }: {
    userId: string;
    scheduleId: string;
    request: UpdateScheduleRequest;
  }) => {
    return await scheduleApi.updateSchedule(userId, scheduleId, request);
  }
);

/**
 * スケジュール削除
 */
export const deleteSchedule = createAsyncThunk(
  'schedule/deleteSchedule',
  async ({ userId, scheduleId }: { userId: string; scheduleId: string }) => {
    await scheduleApi.deleteSchedule(userId, scheduleId);
    return scheduleId;
  }
);

/**
 * 予定を実績に変換（完了にする）
 */
export const markAsDone = createAsyncThunk(
  'schedule/markAsDone',
  async ({ userId, scheduleId }: { userId: string; scheduleId: string }) => {
    return await scheduleApi.markAsDone(userId, scheduleId);
  }
);

const scheduleSlice = createSlice({
  name: 'schedule',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchSchedules
      .addCase(fetchSchedules.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSchedules.fulfilled, (state, action: PayloadAction<Schedule[]>) => {
        state.loading = false;
        state.schedules = action.payload;
      })
      .addCase(fetchSchedules.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch schedules';
      })
      // createSchedule
      .addCase(createSchedule.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createSchedule.fulfilled, (state, action: PayloadAction<Schedule>) => {
        state.loading = false;
        state.schedules.push(action.payload);
      })
      .addCase(createSchedule.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create schedule';
      })
      // updateSchedule
      .addCase(updateSchedule.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateSchedule.fulfilled, (state, action: PayloadAction<Schedule>) => {
        state.loading = false;
        const index = state.schedules.findIndex((s) => s.scheduleId === action.payload.scheduleId);
        if (index !== -1) {
          state.schedules[index] = action.payload;
        }
      })
      .addCase(updateSchedule.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update schedule';
      })
      // deleteSchedule
      .addCase(deleteSchedule.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteSchedule.fulfilled, (state, action: PayloadAction<string>) => {
        state.loading = false;
        state.schedules = state.schedules.filter((s) => s.scheduleId !== action.payload);
      })
      .addCase(deleteSchedule.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete schedule';
      })
      // markAsDone
      .addCase(markAsDone.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(markAsDone.fulfilled, (state, action: PayloadAction<Schedule>) => {
        state.loading = false;
        const index = state.schedules.findIndex((s) => s.scheduleId === action.payload.scheduleId);
        if (index !== -1) {
          state.schedules[index] = action.payload;
        }
      })
      .addCase(markAsDone.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to mark schedule as done';
      });
  },
});

export const { clearError } = scheduleSlice.actions;
export default scheduleSlice.reducer;
