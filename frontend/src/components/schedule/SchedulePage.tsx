import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { AppDispatch, RootState } from '../../store/store';
import {
  fetchSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  convertToCooked,
} from '../../store/slices/scheduleSlice';
import { Schedule, ScheduleType } from '../../types/schedule';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

/**
 * スケジュール管理ページ
 */
const SchedulePage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const { schedules, loading, error } = useSelector((state: RootState) => state.schedule);
  const { user } = useSelector((state: RootState) => state.auth);

  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    date: '',
    type: 'planned' as ScheduleType,
    recipeId: '',
    recipeTitle: '',
    memo: '',
  });
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);

  useEffect(() => {
    // デフォルトで今月のスケジュールを取得
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);

    const start = firstDay.toISOString().split('T')[0];
    const end = lastDay.toISOString().split('T')[0];

    setStartDate(start);
    setEndDate(end);

    if (user?.userId) {
      dispatch(fetchSchedules({ userId: user.userId, params: { startDate: start, endDate: end } }));
    }
  }, [dispatch, user]);

  const handleSearch = () => {
    if (user?.userId && startDate && endDate) {
      dispatch(fetchSchedules({ userId: user.userId, params: { startDate, endDate } }));
    }
  };

  const handleCreate = async () => {
    if (!user?.userId) return;

    await dispatch(
      createSchedule({
        userId: user.userId,
        request: {
          date: formData.date,
          type: formData.type,
          recipeId: formData.recipeId,
          recipeTitle: formData.recipeTitle,
          memo: formData.memo || undefined,
        },
      })
    );

    setShowForm(false);
    setFormData({ date: '', type: 'planned', recipeId: '', recipeTitle: '', memo: '' });
  };

  const handleUpdate = async (scheduleId: string, memo: string) => {
    if (!user?.userId) return;

    await dispatch(
      updateSchedule({
        userId: user.userId,
        scheduleId,
        request: { memo },
      })
    );

    setEditingSchedule(null);
  };

  const handleDelete = async (scheduleId: string) => {
    if (!user?.userId) return;
    if (!confirm(t('schedule.confirmDelete'))) return;

    await dispatch(deleteSchedule({ userId: user.userId, scheduleId }));
  };

  const handleConvertToCooked = async (scheduleId: string) => {
    if (!user?.userId) return;

    await dispatch(convertToCooked({ userId: user.userId, scheduleId }));
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-6">{t('schedule.title')}</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* 検索フォーム */}
      <Card className="p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label htmlFor="startDate">{t('schedule.startDate')}</Label>
            <Input
              id="startDate"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </div>
          <div>
            <Label htmlFor="endDate">{t('schedule.endDate')}</Label>
            <Input
              id="endDate"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
          <div className="flex items-end">
            <Button onClick={handleSearch} disabled={loading}>
              {t('schedule.search')}
            </Button>
          </div>
        </div>
      </Card>

      {/* 新規作成ボタン */}
      <div className="mb-4">
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? t('schedule.cancel') : t('schedule.createNew')}
        </Button>
      </div>

      {/* 作成フォーム */}
      {showForm && (
        <Card className="p-4 mb-6">
          <h2 className="text-xl font-bold mb-4">{t('schedule.createNew')}</h2>
          <div className="space-y-4">
            <div>
              <Label htmlFor="date">{t('schedule.date')}</Label>
              <Input
                id="date"
                type="date"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="type">{t('schedule.type')}</Label>
              <select
                id="type"
                value={formData.type}
                onChange={(e) => setFormData({ ...formData, type: e.target.value as ScheduleType })}
                className="w-full border rounded p-2"
              >
                <option value="planned">{t('schedule.planned')}</option>
                <option value="cooked">{t('schedule.cooked')}</option>
              </select>
            </div>
            <div>
              <Label htmlFor="recipeId">{t('schedule.recipeId')}</Label>
              <Input
                id="recipeId"
                value={formData.recipeId}
                onChange={(e) => setFormData({ ...formData, recipeId: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="recipeTitle">{t('schedule.recipeTitle')}</Label>
              <Input
                id="recipeTitle"
                value={formData.recipeTitle}
                onChange={(e) => setFormData({ ...formData, recipeTitle: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="memo">{t('schedule.memo')}</Label>
              <Input
                id="memo"
                value={formData.memo}
                onChange={(e) => setFormData({ ...formData, memo: e.target.value })}
                maxLength={120}
              />
            </div>
            <Button onClick={handleCreate} disabled={loading}>
              {t('schedule.create')}
            </Button>
          </div>
        </Card>
      )}

      {/* スケジュール一覧 */}
      <div className="space-y-4">
        {loading && <p>{t('common.loading')}</p>}
        {!loading && schedules.length === 0 && <p>{t('schedule.noSchedules')}</p>}
        {schedules.map((schedule) => (
          <Card key={schedule.scheduleId} className="p-4">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <span className="font-bold">{schedule.date}</span>
                  <span
                    className={`px-2 py-1 rounded text-sm ${
                      schedule.type === 'cooked'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-blue-100 text-blue-800'
                    }`}
                  >
                    {t(`schedule.${schedule.type}`)}
                  </span>
                </div>
                <p className="text-lg font-semibold">{schedule.recipeTitle}</p>
                {editingSchedule?.scheduleId === schedule.scheduleId ? (
                  <div className="mt-2">
                    <Input
                      value={editingSchedule.memo || ''}
                      onChange={(e) =>
                        setEditingSchedule({ ...editingSchedule, memo: e.target.value })
                      }
                      maxLength={120}
                    />
                    <div className="mt-2 space-x-2">
                      <Button
                        onClick={() =>
                          handleUpdate(schedule.scheduleId, editingSchedule.memo || '')
                        }
                        size="sm"
                      >
                        {t('common.save')}
                      </Button>
                      <Button onClick={() => setEditingSchedule(null)} size="sm" variant="outline">
                        {t('common.cancel')}
                      </Button>
                    </div>
                  </div>
                ) : (
                  <p className="text-gray-600 mt-1">{schedule.memo}</p>
                )}
              </div>
              <div className="flex gap-2">
                {schedule.type === 'planned' && (
                  <Button
                    onClick={() => handleConvertToCooked(schedule.scheduleId)}
                    size="sm"
                    variant="outline"
                  >
                    {t('schedule.convertToCooked')}
                  </Button>
                )}
                <Button
                  onClick={() => setEditingSchedule(schedule)}
                  size="sm"
                  variant="outline"
                >
                  {t('common.edit')}
                </Button>
                <Button
                  onClick={() => handleDelete(schedule.scheduleId)}
                  size="sm"
                  variant="destructive"
                >
                  {t('common.delete')}
                </Button>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default SchedulePage;
