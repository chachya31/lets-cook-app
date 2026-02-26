import React from 'react';
import { useTranslation } from 'react-i18next';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';

interface LanguageSelectorProps {
  value?: string;
  onChange?: (language: string) => void;
  showLabel?: boolean;
}

/**
 * 言語選択コンポーネント
 * 選択時は言語を変更せず、親コンポーネントに値を渡すのみ
 * 実際の言語切り替えは保存時に行う
 */
export const LanguageSelector: React.FC<LanguageSelectorProps> = ({
  value,
  onChange,
  showLabel = true,
}) => {
  const { t } = useTranslation();

  const handleLanguageChange = (newLanguage: string) => {
    if (onChange) {
      onChange(newLanguage);
    }
  };

  const currentLanguage = value || 'ja';

  return (
    <div className="space-y-2">
      {showLabel && <Label htmlFor="language-selector">{t('profile.language')}</Label>}
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
