import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import { login, register, logout, clearError } from '../store/slices/authSlice';
import { LoginRequest, RegisterRequest } from '../types/user';

/**
 * 認証用カスタムフック
 */
export const useAuth = () => {
  const dispatch = useDispatch<AppDispatch>();
  const auth = useSelector((state: RootState) => state.auth);

  const handleLogin = async (data: LoginRequest) => {
    return dispatch(login(data)).unwrap();
  };

  const handleRegister = async (data: RegisterRequest) => {
    return dispatch(register(data)).unwrap();
  };

  const handleLogout = () => {
    dispatch(logout());
  };

  const handleClearError = () => {
    dispatch(clearError());
  };

  return {
    ...auth,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    clearError: handleClearError,
  };
};
