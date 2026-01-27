import { configureStore } from '@reduxjs/toolkit';
import recipeReducer from './recipeSlice';
import adminReducer from './slices/adminSlice';

export const store = configureStore({
  reducer: {
    recipe: recipeReducer,
    admin: adminReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
