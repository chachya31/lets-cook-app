import React from 'react';
import { useTranslation } from 'react-i18next';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Label } from '../ui/label';

interface LanguageSelectorProps {
  value?: string;
  onChange?: (language: string) => void;
  showLabel?: boolean;
}

/**
 * 言語選択コンポーネント
 */
export const LanguageSelector: React.FC<LanguageSelectorProps> = ({
  value,
  onChange,
  showLabel = true,
}) => {
  const { t, i18n } = useTranslation();

  const handleLanguageChange = (newLanguage: string) => {
    i18n.changeLanguage(newLanguage);
    
    if (onChange) {
      onChange(newLanguage);
    }
    
    localStorage.setItem('preferredLanguage', newLanguage);
  };

  const currentLanguage = value || i18n.language || 'ja';

  return (
    <div className="space-y-2">
      {showLabel && (
        <Label htmlFor="language-selector">{t('profile.language')}</Label>
      )}
      <Select value={currentLanguage} onValueChange={handleLanguageChange}>
        <SelectTrigger id="language-selector" className="w-full">
          <SelectValue placeholder={t('profile.selectLanguage')} />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ja">日本語</SelectItem>
          <SelectItem value="ko">한국어</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
};
