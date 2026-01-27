/**
 * Redux Store (Legacy)
 *
 * 注意: このファイルはZustand移行完了後に削除予定です。
 * 現在は互換性のために空のストアを維持しています。
 */

import { configureStore } from '@reduxjs/toolkit';

export const store = configureStore({
  reducer: {},
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
