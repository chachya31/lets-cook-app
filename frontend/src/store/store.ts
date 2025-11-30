import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import recipeReducer from './recipeSlice';
import reviewReducer from './slices/reviewSlice';
import scheduleReducer from './slices/scheduleSlice';
import shoppingListReducer from './slices/shoppingListSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    recipe: recipeReducer,
    review: reviewReducer,
    schedule: scheduleReducer,
    shoppingList: shoppingListReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
