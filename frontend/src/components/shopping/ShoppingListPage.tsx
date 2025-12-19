import { ShoppingCart } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import {
  addShoppingListItem,
  deleteShoppingListItem,
  fetchShoppingList,
  updateShoppingListItem,
} from '../../store/slices/shoppingListSlice';
import { AppDispatch, RootState } from '../../store/store';
import { formatUnit } from '../../utils/unitHelper';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Checkbox } from '../ui/checkbox';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

const ShoppingListPage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const { items, loading, error } = useSelector((state: RootState) => state.shoppingList);
  const { user } = useSelector((state: RootState) => state.auth);

  const [name, setName] = useState('');
  const [quantity, setQuantity] = useState('');
  const [unit, setUnit] = useState('piece');

  useEffect(() => {
    if (user?.userId) {
      dispatch(fetchShoppingList(user.userId));
    }
  }, [dispatch, user]);

  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.userId || !name || !quantity) return;

    await dispatch(
      addShoppingListItem({
        userId: user.userId,
        request: {
          name,
          quantity: parseFloat(quantity),
          unit,
        },
      })
    );

    // フォームをリセット
    setName('');
    setQuantity('');
    setUnit('piece');
  };

  const handleToggleCheck = async (itemId: string, isChecked: boolean) => {
    if (!user?.userId) return;

    await dispatch(
      updateShoppingListItem({
        userId: user.userId,
        itemId,
        request: { isChecked: !isChecked },
      })
    );
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!user?.userId) return;

    await dispatch(
      deleteShoppingListItem({
        userId: user.userId,
        itemId,
      })
    );
  };

  const uncheckedItems = items.filter((item) => !item.isChecked);
  const checkedItems = items.filter((item) => item.isChecked);

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="flex items-center space-x-3 mb-6">
        <ShoppingCart size={32} className="text-green-600" />
        <h1 className="text-3xl font-bold">{t('shoppingList.title')}</h1>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* アイテム追加フォーム */}
      <Card className="p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">{t('shoppingList.addItem')}</h2>
        <form onSubmit={handleAddItem} className="space-y-4">
          <div>
            <Label htmlFor="name">{t('shoppingList.itemName')}</Label>
            <Input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder={t('shoppingList.itemNamePlaceholder')}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="quantity">{t('shoppingList.quantity')}</Label>
              <Input
                id="quantity"
                type="number"
                step="0.01"
                min="0.01"
                max="9999"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                placeholder="1"
                required
              />
            </div>

            <div>
              <Label htmlFor="unit">{t('shoppingList.unit')}</Label>
              <select
                id="unit"
                value={unit}
                onChange={(e) => setUnit(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="g">g</option>
                <option value="kg">kg</option>
                <option value="ml">ml</option>
                <option value="l">l</option>
                <option value="tbsp">{t('recipe.units.tbsp')}</option>
                <option value="tsp">{t('recipe.units.tsp')}</option>
                <option value="cup">{t('recipe.units.cup')}</option>
                <option value="piece">{t('recipe.units.piece')}</option>
                <option value="pack">{t('recipe.units.pack')}</option>
                <option value="can">{t('recipe.units.can')}</option>
                <option value="bottle">{t('recipe.units.bottle')}</option>
                <option value="slice">{t('recipe.units.slice')}</option>
                <option value="clove">{t('recipe.units.clove')}</option>
                <option value="pinch">{t('recipe.units.pinch')}</option>
                <option value="to_taste">{t('recipe.units.to_taste')}</option>
                <option value="as_needed">{t('recipe.units.as_needed')}</option>
              </select>
            </div>
          </div>

          <Button type="submit" disabled={loading}>
            {loading ? t('common.loading') : t('shoppingList.addButton')}
          </Button>
        </form>
      </Card>

      {/* 未チェックアイテム */}
      {uncheckedItems.length > 0 && (
        <div className="mb-6">
          <h2 className="text-xl font-semibold mb-4">{t('shoppingList.toBuy')}</h2>
          <div className="space-y-2">
            {uncheckedItems.map((item) => (
              <Card key={item.itemId} className="p-4 flex items-center justify-between">
                <div className="flex items-center space-x-4 flex-1">
                  <Checkbox
                    checked={item.isChecked}
                    onCheckedChange={() => handleToggleCheck(item.itemId, item.isChecked)}
                  />
                  <div>
                    <p className="font-medium">{item.name}</p>
                    <p className="text-sm text-gray-600">
                      {item.quantity} {formatUnit(item.unit, t)}
                    </p>
                  </div>
                </div>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => handleDeleteItem(item.itemId)}
                >
                  {t('common.delete')}
                </Button>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* チェック済みアイテム */}
      {checkedItems.length > 0 && (
        <div>
          <h2 className="text-xl font-semibold mb-4">{t('shoppingList.purchased')}</h2>
          <div className="space-y-2">
            {checkedItems.map((item) => (
              <Card key={item.itemId} className="p-4 flex items-center justify-between opacity-60">
                <div className="flex items-center space-x-4 flex-1">
                  <Checkbox
                    checked={item.isChecked}
                    onCheckedChange={() => handleToggleCheck(item.itemId, item.isChecked)}
                  />
                  <div>
                    <p className="font-medium line-through">{item.name}</p>
                    <p className="text-sm text-gray-600">
                      {item.quantity} {formatUnit(item.unit, t)}
                    </p>
                  </div>
                </div>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => handleDeleteItem(item.itemId)}
                >
                  {t('common.delete')}
                </Button>
              </Card>
            ))}
          </div>
        </div>
      )}

      {items.length === 0 && !loading && (
        <div className="text-center py-12 text-gray-500">{t('shoppingList.empty')}</div>
      )}
    </div>
  );
};

export default ShoppingListPage;
