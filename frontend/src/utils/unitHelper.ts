/**
 * 単位コードを表示用ラベルに変換するヘルパー
 */

/**
 * 単位コードから翻訳キーを取得
 * @param unitCode 単位コード（例: 'tbsp', 'g', 'piece'）
 * @returns 翻訳キー（例: 'recipe.units.tbsp'）または単位コードそのまま
 */
export const getUnitTranslationKey = (unitCode: string): string => {
  const unitsWithTranslation = [
    'tbsp',
    'tsp',
    'cup',
    'piece',
    'pack',
    'can',
    'bottle',
    'slice',
    'clove',
    'pinch',
    'to_taste',
    'as_needed',
  ];

  if (unitsWithTranslation.includes(unitCode)) {
    return `recipe.units.${unitCode}`;
  }

  // g, kg, ml, l などはそのまま表示
  return unitCode;
};

/**
 * 単位コードを表示用に変換
 * @param unitCode 単位コード
 * @param t 翻訳関数
 * @returns 表示用の単位文字列
 */
export const formatUnit = (
  unitCode: string,
  t: (key: string) => string
): string => {
  const translationKey = getUnitTranslationKey(unitCode);
  
  // 翻訳キーの場合は翻訳を使用
  if (translationKey.startsWith('recipe.units.')) {
    return t(translationKey);
  }
  
  // それ以外はそのまま返す
  return unitCode;
};
