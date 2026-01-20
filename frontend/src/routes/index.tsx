import { createBrowserRouter } from 'react-router-dom';
import { LandingPage } from '../features/misc/components/LandingPage';
import { LoginPage } from '../features/auth/components/LoginPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <LandingPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
]);
