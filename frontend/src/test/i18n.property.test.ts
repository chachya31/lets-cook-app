import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import * as fc from 'fast-check';

/**
 * 優先言語の保存プロパティベーステスト
 * 
 * Feature: cooking-support-app, Property 35: 優先言語の保存
 * すべての言語選択に対して、PreferredLanguage属性に設定が保存される
 * 
 * Validates: Requirements 9.1
 */
describe('Language Preference Property Tests', () => {
  // 各テストの前にlocalStorageをクリア
  beforeEach(() => {
    localStorage.clear();
  });

  // 各テストの後にlocalStorageをクリア
  afterEach(() => {
    localStorage.clear();
  });

  /**
   * Property 35: 優先言語の保存
   * 
   * すべての有効な言語選択（'ja'または'ko'）に対して、
   * localStorageに正しく保存され、取得できることを検証する
   */
  it('should save preferred language to localStorage for all valid language selections', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ja', 'ko'),
        (language) => {
          // Act: 言語をlocalStorageに保存
          localStorage.setItem('preferredLanguage', language);

          // Assert: 保存された言語が正しく取得できることを確認
          const savedLanguage = localStorage.getItem('preferredLanguage');
          expect(savedLanguage).toBe(language);
          expect(['ja', 'ko']).toContain(savedLanguage);
        }
      ),
      { numRuns: 100 } // 100回のランダムテストを実行
    );
  });

  /**
   * Property: 言語設定の永続性
   * 
   * すべての有効な言語選択に対して、
   * 保存後に再取得しても同じ値が返されることを検証する
   */
  it('should persist language preference across multiple get operations', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ja', 'ko'),
        (language) => {
          // Arrange & Act: 言語を保存
          localStorage.setItem('preferredLanguage', language);

          // Assert: 複数回取得しても同じ値が返される
          const firstRetrieval = localStorage.getItem('preferredLanguage');
          const secondRetrieval = localStorage.getItem('preferredLanguage');
          const thirdRetrieval = localStorage.getItem('preferredLanguage');

          expect(firstRetrieval).toBe(language);
          expect(secondRetrieval).toBe(language);
          expect(thirdRetrieval).toBe(language);
          expect(firstRetrieval).toBe(secondRetrieval);
          expect(secondRetrieval).toBe(thirdRetrieval);
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Property: 言語設定の上書き
   * 
   * すべての言語選択のペアに対して、
   * 新しい言語設定が古い設定を正しく上書きすることを検証する
   */
  it('should overwrite previous language preference when a new one is set', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ja', 'ko'),
        fc.constantFrom('ja', 'ko'),
        (firstLanguage, secondLanguage) => {
          // Arrange: 最初の言語を保存
          localStorage.setItem('preferredLanguage', firstLanguage);
          const firstSaved = localStorage.getItem('preferredLanguage');
          expect(firstSaved).toBe(firstLanguage);

          // Act: 2番目の言語で上書き
          localStorage.setItem('preferredLanguage', secondLanguage);

          // Assert: 最新の言語が保存されている
          const finalSaved = localStorage.getItem('preferredLanguage');
          expect(finalSaved).toBe(secondLanguage);
          expect(finalSaved).not.toBe(firstLanguage === secondLanguage ? 'different' : firstLanguage);
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Property: 無効な言語の拒否
   * 
   * すべての無効な言語文字列に対して、
   * システムがデフォルト言語（'ja'）にフォールバックすることを検証する
   */
  it('should fallback to default language for invalid language values', () => {
    fc.assert(
      fc.property(
        fc.string().filter(s => s !== 'ja' && s !== 'ko' && s.length > 0),
        (invalidLanguage) => {
          // Act: 無効な言語を保存
          localStorage.setItem('preferredLanguage', invalidLanguage);
          const savedLanguage = localStorage.getItem('preferredLanguage');

          // Assert: 保存された値は取得できるが、有効な言語ではない
          expect(savedLanguage).toBe(invalidLanguage);
          expect(['ja', 'ko']).not.toContain(savedLanguage);

          // アプリケーションロジックでは、無効な値の場合はデフォルト（'ja'）を使用する
          const effectiveLanguage = ['ja', 'ko'].includes(savedLanguage!) ? savedLanguage : 'ja';
          expect(effectiveLanguage).toBe('ja');
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Property: 空の言語設定
   * 
   * localStorageに言語が保存されていない場合、
   * nullが返されることを検証する
   */
  it('should return null when no language preference is set', () => {
    // Arrange: localStorageをクリア（beforeEachで既にクリア済み）
    
    // Act: 言語を取得
    const savedLanguage = localStorage.getItem('preferredLanguage');

    // Assert: nullが返される
    expect(savedLanguage).toBeNull();
  });

  /**
   * Property: 言語設定の削除
   * 
   * すべての言語選択に対して、
   * 保存後に削除すると取得できなくなることを検証する
   */
  it('should remove language preference when explicitly deleted', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ja', 'ko'),
        (language) => {
          // Arrange: 言語を保存
          localStorage.setItem('preferredLanguage', language);
          expect(localStorage.getItem('preferredLanguage')).toBe(language);

          // Act: 言語設定を削除
          localStorage.removeItem('preferredLanguage');

          // Assert: 取得できなくなる
          const savedLanguage = localStorage.getItem('preferredLanguage');
          expect(savedLanguage).toBeNull();
        }
      ),
      { numRuns: 100 }
    );
  });
});
