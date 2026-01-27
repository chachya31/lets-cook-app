import { configureStore } from '@reduxjs/toolkit';
import recipeReducer from './recipeSlice';
import adminReducer from './slices/adminSlice';
import scheduleReducer from './slices/scheduleSlice';
import shoppingListReducer from './slices/shoppingListSlice';

export const store = configureStore({
  reducer: {
    recipe: recipeReducer,
    schedule: scheduleReducer,
    shoppingList: shoppingListReducer,
    admin: adminReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
