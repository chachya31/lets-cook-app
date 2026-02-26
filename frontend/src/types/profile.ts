/**
 * プロフィール関連の型定義
 */

export interface UpdateProfileRequest {
  nickname: string;
  displayName: string;
  preferredLanguage: 'ja' | 'ko';
  timezone: string;
  marketingOptOut: boolean;
}
