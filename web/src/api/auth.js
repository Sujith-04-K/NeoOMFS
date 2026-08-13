import { api } from './client';
import { setToken, setRefreshToken, setUser } from '../utils/auth';

// client.js now returns the inner .data from ApiResponse<T> directly
// So for AuthResponse: { accessToken, refreshToken, tokenType, user }

export const login = async (email, password) => {
  const payload = await api.post('/auth/login', { email, password });
  if (payload?.accessToken) setToken(payload.accessToken);
  if (payload?.refreshToken) setRefreshToken(payload.refreshToken);
  if (payload?.user) setUser(payload.user);
  return payload;
};

export const register = async (data) => {
  const payload = await api.post('/auth/register', data);
  if (payload?.accessToken) setToken(payload.accessToken);
  if (payload?.refreshToken) setRefreshToken(payload.refreshToken);
  if (payload?.user) setUser(payload.user);
  return payload;
};

export const getProfile = async () => {
  return await api.get('/auth/me');
};

export const updateProfile = async (data) => {
  return await api.put('/auth/me', data);
};

export const forgotPassword = async (email) => {
  return await api.post('/auth/forgot-password', { email });
};

export const resetPassword = async (email, otp, newPassword) => {
  return await api.post('/auth/reset-password', { email, otp, newPassword });
};

export const changePassword = async (currentPassword, newPassword) => {
  return await api.put('/auth/change-password', { currentPassword, newPassword });
};
