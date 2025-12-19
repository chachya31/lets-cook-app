import { Save, User, X } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { updateProfile, uploadProfileImage } from '../../api/profileApi';
import { getUserProfile } from '../../api/userApi';
import { RootState } from '../../store/store';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { ImageUploader } from './ImageUploader';
import { LanguageSelector } from './LanguageSelector';

/**
 * プロフィール編集ページ
 */
const ProfileEditPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const currentUser = useSelector((state: RootState) => state.auth.user);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // フォーム状態
  const [nickname, setNickname] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [language, setLanguage] = useState<'ja' | 'ko'>('ja');
  const [timezone, setTimezone] = useState('Asia/Tokyo');
  const [marketingOptOut, setMarketingOptOut] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState<string | undefined>();
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);

  // 初期データ読み込み
  useEffect(() => {
    if (!currentUser?.userId) {
      navigate('/login');
      return;
    }

    const loadProfile = async () => {
      try {
        const user = await getUserProfile(currentUser.userId);
        setNickname(user.nickname);
        setDisplayName(user.displayName);
        setLanguage(user.preferredLanguage);
        setTimezone(user.timezone);
        setMarketingOptOut(user.marketingOptOut);
        setProfileImageUrl(user.profileImageUrl);
      } catch (err) {
        setError(t('profile.errors.loadFailed'));
      }
    };

    loadProfile();
  }, [currentUser, navigate, t]);

  /**
   * 画像選択ハンドラー
   */
  const handleImageSelect = (file: File) => {
    setSelectedImageFile(file);
    setError(null);
  };

  /**
   * 画像削除ハンドラー
   */
  const handleImageRemove = () => {
    setSelectedImageFile(null);
    setProfileImageUrl(undefined);
  };

  /**
   * フォーム送信ハンドラー
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentUser?.userId) {
      setError(t('profile.errors.notLoggedIn'));
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      // プロフィール情報を更新
      await updateProfile(currentUser.userId, {
        nickname,
        displayName,
        preferredLanguage: language,
        timezone,
        marketingOptOut,
      });

      // 画像が選択されている場合はアップロード
      if (selectedImageFile) {
        const updatedUser = await uploadProfileImage(currentUser.userId, selectedImageFile);
        setProfileImageUrl(updatedUser.profileImageUrl);
        setSelectedImageFile(null);
      }

      setSuccess(true);
      setTimeout(() => {
        navigate('/dashboard');
      }, 2000);
    } catch (err) {
      setError(t('profile.errors.updateFailed'));
    } finally {
      setLoading(false);
    }
  };

  /**
   * キャンセルハンドラー
   */
  const handleCancel = () => {
    navigate('/dashboard');
  };

  if (!currentUser) {
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <div className="flex items-center space-x-3 mb-6">
        <User size={32} className="text-green-600" />
        <h1 className="text-3xl font-bold">{t('profile.edit.title')}</h1>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {t('profile.edit.success')}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* プロフィール画像 */}
        <Card>
          <CardHeader>
            <CardTitle>{t('profile.profileImage')}</CardTitle>
          </CardHeader>
          <CardContent>
            <ImageUploader
              currentImageUrl={profileImageUrl}
              onImageSelect={handleImageSelect}
              onImageRemove={handleImageRemove}
              maxSizeMB={5}
            />
          </CardContent>
        </Card>

        {/* 基本情報 */}
        <Card>
          <CardHeader>
            <CardTitle>{t('profile.basicInfo')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="email">{t('profile.email')}</Label>
              <Input
                id="email"
                type="email"
                value={currentUser.email}
                disabled
                className="bg-gray-100"
              />
              <p className="text-sm text-gray-500 mt-1">{t('profile.emailNotEditable')}</p>
            </div>

            <div>
              <Label htmlFor="nickname">{t('profile.nickname')}</Label>
              <Input
                id="nickname"
                value={nickname}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNickname(e.target.value)}
                required
                maxLength={50}
              />
            </div>

            <div>
              <Label htmlFor="displayName">{t('profile.displayName')}</Label>
              <Input
                id="displayName"
                value={displayName}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setDisplayName(e.target.value)
                }
                required
                maxLength={50}
              />
            </div>

            <LanguageSelector
              value={language}
              onChange={(lang) => setLanguage(lang as 'ja' | 'ko')}
              showLabel={true}
            />

            <div>
              <Label htmlFor="timezone">{t('profile.timezone')}</Label>
              <Input id="timezone" value={timezone} disabled className="bg-gray-100" />
              <p className="text-sm text-gray-500 mt-1">{t('profile.timezoneNotEditable')}</p>
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="marketingOptOut"
                checked={marketingOptOut}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setMarketingOptOut(e.target.checked)
                }
                className="w-4 h-4"
              />
              <Label htmlFor="marketingOptOut" className="cursor-pointer">
                {t('profile.marketingOptOut')}
              </Label>
            </div>
          </CardContent>
        </Card>

        {/* アクションボタン */}
        <div className="flex gap-2 justify-end">
          <Button
            type="button"
            variant="outline"
            onClick={handleCancel}
            disabled={loading}
            className="flex items-center space-x-2"
          >
            <X size={16} />
            <span>{t('common.cancel')}</span>
          </Button>
          <Button type="submit" disabled={loading} className="flex items-center space-x-2">
            <Save size={16} />
            <span>{loading ? t('common.saving') : t('common.save')}</span>
          </Button>
        </div>
      </form>
    </div>
  );
};

export default ProfileEditPage;
