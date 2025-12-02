import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { AppDispatch, RootState } from '../../store/store';
import { suspendUser, deleteUserByAdmin } from '../../store/slices/adminSlice';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

/**
 * ユーザー管理ページ
 */
const UserManagementPage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const { loading, error } = useSelector((state: RootState) => state.admin);

  const [userId, setUserId] = useState('');
  const [actionResult, setActionResult] = useState<string | null>(null);

  const handleSuspendUser = async () => {
    if (!userId.trim()) {
      setActionResult(t('admin.users.error.userIdRequired'));
      return;
    }

    try {
      await dispatch(suspendUser(userId)).unwrap();
      setActionResult(t('admin.users.success.suspended'));
      setUserId('');
    } catch (err) {
      setActionResult(t('admin.users.error.suspendFailed'));
    }
  };

  const handleDeleteUser = async () => {
    if (!userId.trim()) {
      setActionResult(t('admin.users.error.userIdRequired'));
      return;
    }

    if (!window.confirm(t('admin.users.confirm.delete'))) {
      return;
    }

    try {
      await dispatch(deleteUserByAdmin(userId)).unwrap();
      setActionResult(t('admin.users.success.deleted'));
      setUserId('');
    } catch (err) {
      setActionResult(t('admin.users.error.deleteFailed'));
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">{t('admin.users.title')}</h1>

      {/* エラー表示 */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {/* アクション結果表示 */}
      {actionResult && (
        <div className="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded mb-6">
          {actionResult}
        </div>
      )}

      {/* ユーザー管理フォーム */}
      <Card className="p-6">
        <h2 className="text-xl font-semibold mb-4">{t('admin.users.manageUser')}</h2>

        <div className="space-y-4">
          <div>
            <Label htmlFor="userId">{t('admin.users.userId')}</Label>
            <Input
              id="userId"
              type="text"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              placeholder={t('admin.users.userIdPlaceholder')}
              disabled={loading}
            />
          </div>

          <div className="flex gap-4">
            <Button
              onClick={handleSuspendUser}
              disabled={loading || !userId.trim()}
              variant="outline"
              className="flex-1"
            >
              {loading ? t('common.loading') : t('admin.users.suspend')}
            </Button>

            <Button
              onClick={handleDeleteUser}
              disabled={loading || !userId.trim()}
              variant="destructive"
              className="flex-1"
            >
              {loading ? t('common.loading') : t('admin.users.delete')}
            </Button>
          </div>
        </div>

        {/* 注意事項 */}
        <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded">
          <p className="text-sm text-yellow-800">
            <strong>{t('admin.users.warning.title')}</strong>
          </p>
          <ul className="list-disc list-inside text-sm text-yellow-700 mt-2">
            <li>{t('admin.users.warning.suspend')}</li>
            <li>{t('admin.users.warning.delete')}</li>
          </ul>
        </div>
      </Card>
    </div>
  );
};

export default UserManagementPage;
