import { act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as scheduleApi from '../../api/scheduleApi';
import { useScheduleStore } from '../../store/scheduleStore';

vi.mock('../../api/scheduleApi');

describe('scheduleStore', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useScheduleStore.setState({
      schedules: [],
      loading: false,
      error: null,
    });
  });

  const mockSchedule = {
    scheduleId: 'schedule-1',
    userId: 'user-1',
    date: '2025-01-15',
    isDone: false,
    recipeId: 'recipe-1',
    recipeTitle: 'カレーライス',
    memo: 'メモ',
    createdAt: '2025-01-01T10:00:00Z',
  };

  describe('fetchSchedules', () => {
    it('should fetch schedules successfully', async () => {
      const mockSchedules = [mockSchedule];
      vi.mocked(scheduleApi.getSchedules).mockResolvedValue(mockSchedules);

      await act(async () => {
        await useScheduleStore.getState().fetchSchedules('user-1', {
          startDate: '2025-01-01',
          endDate: '2025-01-31',
        });
      });

      const state = useScheduleStore.getState();
      expect(state.schedules).toEqual(mockSchedules);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should set error on fetch failure', async () => {
      vi.mocked(scheduleApi.getSchedules).mockRejectedValue(new Error('Failed to fetch'));

      await act(async () => {
        await useScheduleStore.getState().fetchSchedules('user-1', {
          startDate: '2025-01-01',
          endDate: '2025-01-31',
        });
      });

      const state = useScheduleStore.getState();
      expect(state.error).toBe('Failed to fetch');
    });
  });

  describe('createSchedule', () => {
    it('should create schedule successfully', async () => {
      vi.mocked(scheduleApi.createSchedule).mockResolvedValue(mockSchedule);

      await act(async () => {
        await useScheduleStore.getState().createSchedule('user-1', {
          date: '2025-01-15',
          recipeId: 'recipe-1',
          recipeTitle: 'カレーライス',
        });
      });

      const state = useScheduleStore.getState();
      expect(state.schedules).toContainEqual(mockSchedule);
      expect(state.loading).toBe(false);
    });
  });

  describe('updateSchedule', () => {
    it('should update schedule successfully', async () => {
      useScheduleStore.setState({ schedules: [mockSchedule] });

      const updatedSchedule = { ...mockSchedule, memo: '更新メモ' };
      vi.mocked(scheduleApi.updateSchedule).mockResolvedValue(updatedSchedule);

      await act(async () => {
        await useScheduleStore.getState().updateSchedule('user-1', 'schedule-1', {
          memo: '更新メモ',
        });
      });

      const state = useScheduleStore.getState();
      expect(state.schedules[0].memo).toBe('更新メモ');
    });
  });

  describe('deleteSchedule', () => {
    it('should delete schedule successfully', async () => {
      useScheduleStore.setState({ schedules: [mockSchedule] });
      vi.mocked(scheduleApi.deleteSchedule).mockResolvedValue(undefined);

      await act(async () => {
        await useScheduleStore.getState().deleteSchedule('user-1', 'schedule-1');
      });

      const state = useScheduleStore.getState();
      expect(state.schedules).toHaveLength(0);
    });
  });

  describe('markAsDone', () => {
    it('should mark schedule as done successfully', async () => {
      useScheduleStore.setState({ schedules: [mockSchedule] });
      const doneSchedule = { ...mockSchedule, isDone: true };
      vi.mocked(scheduleApi.markAsDone).mockResolvedValue(doneSchedule);

      await act(async () => {
        await useScheduleStore.getState().markAsDone('user-1', 'schedule-1');
      });

      const state = useScheduleStore.getState();
      expect(state.schedules[0].isDone).toBe(true);
    });
  });

  describe('clearError', () => {
    it('should clear error', () => {
      useScheduleStore.setState({ error: 'some error' });

      act(() => {
        useScheduleStore.getState().clearError();
      });

      expect(useScheduleStore.getState().error).toBeNull();
    });
  });
});
