import React from 'react';
import { useTranslation } from 'react-i18next';

/**
 * フッターコンポーネント
 * アプリケーション全体のフッター
 */
const Footer: React.FC = () => {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-50 border-t border-gray-200 mt-auto">
      <div className="container mx-auto px-4 py-6">
        <div className="flex flex-col md:flex-row justify-between items-center">
          {/* アプリケーション名 */}
          <div className="text-gray-600 mb-4 md:mb-0">
            © {currentYear} {t('app.title')}
          </div>

          {/* リンク */}
          <div className="flex space-x-6 text-sm text-gray-600">
            <a
              href="#"
              className="hover:text-green-600 transition-colors"
              onClick={(e) => e.preventDefault()}
            >
              {t('common.comingSoon')}
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
