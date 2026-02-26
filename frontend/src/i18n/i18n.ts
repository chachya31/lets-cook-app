import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ja from './locales/ja.json';
import ko from './locales/ko.json';

// 言語優先順位: ログインユーザーの優先言語 > localStorage > ブラウザ言語 > デフォルト（日本語）
const getInitialLanguage = (): string => {
  // 1. ログインユーザーの優先言語（最優先）
  const userJson = localStorage.getItem('user');
  if (userJson) {
    try {
      const user = JSON.parse(userJson);
      if (user.preferredLanguage && ['ja', 'ko'].includes(user.preferredLanguage)) {
        return user.preferredLanguage;
      }
    } catch {
      // JSON解析エラーは無視
    }
  }

  // 2. localStorageの優先言語
  const savedLanguage = localStorage.getItem('preferredLanguage');
  if (savedLanguage && ['ja', 'ko'].includes(savedLanguage)) {
    return savedLanguage;
  }

  // 3. ブラウザの言語設定
  const browserLanguage = navigator.language.split('-')[0];
  if (['ja', 'ko'].includes(browserLanguage)) {
    return browserLanguage;
  }

  // 4. デフォルト言語（日本語）
  return 'ja';
};

i18n.use(initReactI18next).init({
  resources: {
    ja: { translation: ja },
    ko: { translation: ko },
  },
  lng: getInitialLanguage(),
  fallbackLng: 'ja',
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
