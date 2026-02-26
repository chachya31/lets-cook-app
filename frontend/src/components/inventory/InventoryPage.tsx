import { AlertTriangle, Package, Trash2 } from 'lucide-react';
import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
    deleteInventoryItem,
    getInventory,
    InventoryItem,
    updateInventoryItem,
} from '../../api/inventoryApi';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

const InventoryPage: React.FC = () => {
  const { t } = useTranslation();
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sortByExpiry, setSortByExpiry] = useState(true);
  const [editingItem, setEditingItem] = useState<string | null>(null);
  const [editExpiryDate, setEditExpiryDate] = useState('');

  const loadInventory = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getInventory(sortByExpiry);
      setItems(data);
      setError(null);
    } catch {
      setError(t('inventory.fetchError'));
    } finally {
      setLoading(false);
    }
  }, [sortByExpiry, t]);

  useEffect(() => {
    loadInventory();
  }, [loadInventory]);

  const handleDelete = async (itemId: string) => {
    if (!confirm(t('inventory.confirmDelete'))) return;

    try {
      await deleteInventoryItem(itemId);
      setItems((prev) => prev.filter((item) => item.itemId !== itemId));
    } catch {
      setError(t('inventory.deleteError'));
    }
  };

  const handleEditExpiry = (item: InventoryItem) => {
    setEditingItem(item.itemId);
    setEditExpiryDate(item.expiryDate || '');
  };

  const handleSaveExpiry = async (itemId: string) => {
    try {
      const updated = await updateInventoryItem(itemId, {
        expiryDate: editExpiryDate || undefined,
      });
      setItems((prev) => prev.map((item) => (item.itemId === itemId ? updated : item)));
      setEditingItem(null);
      setEditExpiryDate('');
    } catch {
      setError(t('inventory.updateError'));
    }
  };

  const handleCancelEdit = () => {
    setEditingItem(null);
    setEditExpiryDate('');
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return t('inventory.notSet');
    const date = new Date(dateStr);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getExpiryStatusClass = (item: InventoryItem) => {
    if (item.isExpired) return 'bg-red-50 border-red-200';
    if (item.isExpiringSoon) return 'bg-yellow-50 border-yellow-200';
    return '';
  };

  const getExpiryBadge = (item: InventoryItem) => {
    if (item.isExpired) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium text-red-700 bg-red-100 rounded">
          <AlertTriangle size={12} />
          {t('inventory.expired')}
        </span>
      );
    }
    if (item.isExpiringSoon) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium text-yellow-700 bg-yellow-100 rounded">
          <AlertTriangle size={12} />
          {t('inventory.expiringSoon')}
        </span>
      );
    }
    return null;
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center space-x-3">
          <Package size={32} className="text-blue-600" />
          <h1 className="text-3xl font-bold">{t('inventory.title')}</h1>
        </div>
        <div className="flex items-center gap-2">
          <Label htmlFor="sortByExpiry" className="text-sm">
            {t('inventory.sortByExpiry')}
          </Label>
          <input
            id="sortByExpiry"
            type="checkbox"
            checked={sortByExpiry}
            onChange={(e) => setSortByExpiry(e.target.checked)}
            className="h-4 w-4"
          />
        </div>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      <p className="text-gray-600 mb-6">{t('inventory.description')}</p>

      {loading ? (
        <div className="text-center py-12 text-gray-500">{t('common.loading')}</div>
      ) : items.length === 0 ? (
        <div className="text-center py-12 text-gray-500">{t('inventory.empty')}</div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <Card
              key={item.itemId}
              className={`p-4 flex items-center justify-between ${getExpiryStatusClass(item)}`}
            >
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <p className="font-medium text-lg">{item.name}</p>
                  {getExpiryBadge(item)}
                </div>
                <p className="text-sm text-gray-600">
                  {item.quantity} {item.unit}
                </p>
                {editingItem === item.itemId ? (
                  <div className="mt-2 flex items-center gap-2">
                    <Input
                      type="date"
                      value={editExpiryDate}
                      onChange={(e) => setEditExpiryDate(e.target.value)}
                      className="w-40"
                    />
                    <Button size="sm" onClick={() => handleSaveExpiry(item.itemId)}>
                      {t('common.save')}
                    </Button>
                    <Button size="sm" variant="outline" onClick={handleCancelEdit}>
                      {t('common.cancel')}
                    </Button>
                  </div>
                ) : (
                  <p className="text-sm text-gray-500 mt-1">
                    {t('inventory.expiryDate')}:{' '}
                    <button
                      onClick={() => handleEditExpiry(item)}
                      className="text-blue-600 hover:underline"
                    >
                      {formatDate(item.expiryDate)}
                    </button>
                  </p>
                )}
              </div>
              <Button
                variant="destructive"
                size="sm"
                onClick={() => handleDelete(item.itemId)}
                className="ml-4"
              >
                <Trash2 size={16} />
              </Button>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default InventoryPage;
