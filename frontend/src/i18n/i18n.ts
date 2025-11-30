import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ja from './locales/ja.json';
import ko from './locales/ko.json';

// localStorageから優先言語を取得、なければブラウザの言語設定を使用
const getInitialLanguage = (): string => {
  const savedLanguage = localStorage.getItem('preferredLanguage');
  if (savedLanguage && ['ja', 'ko'].includes(savedLanguage)) {
    return savedLanguage;
  }
  
  const browserLanguage = navigator.language.split('-')[0];
  if (['ja', 'ko'].includes(browserLanguage)) {
    return browserLanguage;
  }
  
  return 'ja';
};

i18n
  .use(initReactI18next)
  .init({
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
