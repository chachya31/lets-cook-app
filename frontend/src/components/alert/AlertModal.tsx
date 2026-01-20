import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { checkAlert } from '../../api/alertApi';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../ui/dialog';

const ALERT_STORAGE_KEY = 'alert_dismissed_date';

/**
 * サボり防止アラートモーダル
 * 最終料理日から3日経過した場合に表示
 */
export const AlertModal: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [message, setMessage] = useState<string>('');
  const user = useAuthStore((state) => state.user);
  const userId = user?.userId;

  useEffect(() => {
    const checkAndShowAlert = async () => {
      if (!userId) return;

      // 同日の再表示を防止
      const dismissedDate = localStorage.getItem(ALERT_STORAGE_KEY);
      const today = new Date().toISOString().split('T')[0];

      if (dismissedDate === today) {
        return;
      }

      try {
        const response = await checkAlert(userId);

        if (response.shouldShow && response.message) {
          setMessage(response.message);
          setIsOpen(true);
        }
      } catch (error) {
        console.error('Failed to check alert:', error);
      }
    };

    checkAndShowAlert();
  }, [userId]);

  /**
   * モーダルを閉じる
   */
  const handleClose = () => {
    // localStorageに当日フラグを保存
    const today = new Date().toISOString().split('T')[0];
    localStorage.setItem(ALERT_STORAGE_KEY, today);
    setIsOpen(false);
  };

  /**
   * クイック料理登録
   * スケジュール画面に遷移して料理実績を登録
   */
  const handleQuickRegister = () => {
    handleClose();
    navigate('/schedules');
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('alert.title')}</DialogTitle>
          <DialogDescription>{message}</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            {t('alert.dismiss')}
          </Button>
          <Button onClick={handleQuickRegister}>{t('alert.quickRegister')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
