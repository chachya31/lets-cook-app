import { Calendar } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import { useScrollToMessage } from '../../hooks/useScrollToMessage';
import {
  createSchedule,
  deleteSchedule,
  fetchSchedules,
  markAsDone,
  updateSchedule,
} from '../../store/slices/scheduleSlice';
import { AppDispatch, RootState } from '../../store/store';
import { Recipe } from '../../types/recipe';
import { Schedule } from '../../types/schedule';
import { MessageDisplay } from '../common/MessageDisplay';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import RecipeSelectModal from './RecipeSelectModal';

/**
 * スケジュール管理ページ
 */
const SchedulePage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const { messageRef, scrollToMessage } = useScrollToMessage();
  const { schedules, loading, error } = useSelector((state: RootState) => state.schedule);
  const { user } = useSelector((state: RootState) => state.auth);

  // エラー発生時にスクロール
  useEffect(() => {
    if (error) {
      scrollToMessage();
    }
  }, [error, scrollToMessage]);

  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    date: '',
    recipeId: '',
    recipeTitle: '',
    memo: '',
  });
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
  const [showRecipeModal, setShowRecipeModal] = useState(false);

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
          recipeId: formData.recipeId,
          recipeTitle: formData.recipeTitle,
          memo: formData.memo || undefined,
        },
      })
    );

    setShowForm(false);
    setFormData({ date: '', recipeId: '', recipeTitle: '', memo: '' });
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

  const handleMarkAsDone = async (scheduleId: string) => {
    if (!user?.userId) return;

    await dispatch(markAsDone({ userId: user.userId, scheduleId }));
  };

  const handleRecipeSelect = (recipe: Recipe) => {
    setFormData({
      ...formData,
      recipeId: recipe.recipeId,
      recipeTitle: recipe.title,
    });
  };

  return (
    <div className="container mx-auto p-4">
      <div className="flex items-center space-x-3 mb-6">
        <Calendar size={32} className="text-green-600" />
        <h1 className="text-3xl font-bold">{t('schedule.title')}</h1>
      </div>

      <MessageDisplay ref={messageRef} error={error} />

      {/* 検索フォーム */}
      <Card className="p-4 mb-6">
        <div className="space-y-4">
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
          <div>
            <Button onClick={handleSearch} disabled={loading} className="w-full">
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
              <Label>{t('schedule.recipe')}</Label>
              <div className="flex gap-2">
                <Input
                  value={formData.recipeTitle}
                  readOnly
                  placeholder={t('schedule.selectRecipePlaceholder')}
                  className="flex-1 bg-gray-50"
                />
                <Button type="button" variant="outline" onClick={() => setShowRecipeModal(true)}>
                  {t('schedule.selectRecipe')}
                </Button>
              </div>
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
                      schedule.isDone ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'
                    }`}
                  >
                    {t(schedule.isDone ? 'schedule.cooked' : 'schedule.planned')}
                  </span>
                </div>
                <p className="text-lg font-semibold">{schedule.recipeTitle}</p>
                {editingSchedule?.scheduleId === schedule.scheduleId ? (
                  <div className="mt-2">
                    <Label className="text-sm text-gray-500">{t('schedule.memo')}</Label>
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
                  schedule.memo && (
                    <div className="mt-1">
                      <span className="text-sm text-gray-500">{t('schedule.memo')}: </span>
                      <span className="text-gray-600">{schedule.memo}</span>
                    </div>
                  )
                )}
              </div>
              <div className="flex gap-2">
                {!schedule.isDone && (
                  <Button
                    onClick={() => handleMarkAsDone(schedule.scheduleId)}
                    size="sm"
                    variant="outline"
                  >
                    {t('schedule.markAsDone')}
                  </Button>
                )}
                <Button onClick={() => setEditingSchedule(schedule)} size="sm" variant="outline">
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

      {/* レシピ選択モーダル */}
      <RecipeSelectModal
        open={showRecipeModal}
        onClose={() => setShowRecipeModal(false)}
        onSelect={handleRecipeSelect}
      />
    </div>
  );
};

export default SchedulePage;
