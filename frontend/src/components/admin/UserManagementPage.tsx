import { Users } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { deleteUserByAdmin, fetchAllUsers, suspendUser } from '../../store/slices/adminSlice';
import { AppDispatch, RootState } from '../../store/store';
import LoadingSkeleton from '../common/LoadingSkeleton';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

/**
 * ユーザー管理ページ
 */
const UserManagementPage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { users, loading, error } = useSelector((state: RootState) => state.admin);
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const isAdmin = currentUser?.roles?.includes('Admins') ?? false;

  const [actionResult, setActionResult] = useState<string | null>(null);

  useEffect(() => {
    if (!isAdmin) {
      navigate('/dashboard');
      return;
    }
    dispatch(fetchAllUsers());
  }, [dispatch, isAdmin, navigate]);

  const handleSuspendUser = async (userId: string) => {
    if (!window.confirm(t('admin.users.confirm.suspend'))) {
      return;
    }
    try {
      await dispatch(suspendUser(userId)).unwrap();
      setActionResult(t('admin.users.success.suspended'));
      dispatch(fetchAllUsers());
    } catch {
      setActionResult(t('admin.users.error.suspendFailed'));
    }
  };

  const handleDeleteUser = async (userId: string) => {
    if (!window.confirm(t('admin.users.confirm.delete'))) {
      return;
    }
    try {
      await dispatch(deleteUserByAdmin(userId)).unwrap();
      setActionResult(t('admin.users.success.deleted'));
      dispatch(fetchAllUsers());
    } catch {
      setActionResult(t('admin.users.error.deleteFailed'));
    }
  };

  if (loading && users.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSkeleton type="card" />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center space-x-3 mb-8">
        <Users size={32} className="text-green-600" />
        <h1 className="text-3xl font-bold">{t('admin.users.title')}</h1>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {actionResult && (
        <div className="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded mb-6">
          {actionResult}
          <button
            onClick={() => setActionResult(null)}
            className="ml-4 text-blue-500 hover:text-blue-700"
          >
            ×
          </button>
        </div>
      )}

      <Card className="p-6">
        <h2 className="text-xl font-semibold mb-4">{t('admin.users.userList')}</h2>

        {users.length === 0 ? (
          <p className="text-gray-500">{t('admin.users.noUsers')}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.table.user')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.table.email')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.table.role')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.table.createdAt')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.table.actions')}
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {users.map((user) => (
                  <tr key={user.userId}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {user.profileImageUrl ? (
                          <img
                            src={user.profileImageUrl}
                            alt={user.nickname}
                            className="h-10 w-10 rounded-full object-cover"
                          />
                        ) : (
                          <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                            <span className="text-gray-500 font-medium">
                              {user.nickname.charAt(0).toUpperCase()}
                            </span>
                          </div>
                        )}
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{user.nickname}</div>
                          <div className="text-sm text-gray-500">{user.displayName}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {user.roles && user.roles.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {user.roles.map((role) => (
                            <span
                              key={role}
                              className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                                role === 'Admins'
                                  ? 'bg-purple-100 text-purple-800'
                                  : 'bg-gray-100 text-gray-800'
                              }`}
                            >
                              {role}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-sm text-gray-400">-</span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(user.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {user.userId !== currentUser?.userId && (
                        <div className="flex gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleSuspendUser(user.userId)}
                            disabled={loading}
                          >
                            {t('admin.users.suspend')}
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDeleteUser(user.userId)}
                            disabled={loading}
                          >
                            {t('admin.users.delete')}
                          </Button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded">
        <p className="text-sm text-yellow-800">
          <strong>{t('admin.users.warning.title')}</strong>
        </p>
        <ul className="list-disc list-inside text-sm text-yellow-700 mt-2">
          <li>{t('admin.users.warning.suspend')}</li>
          <li>{t('admin.users.warning.delete')}</li>
        </ul>
      </div>
    </div>
  );
};

export default UserManagementPage;
