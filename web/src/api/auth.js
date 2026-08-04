import { api } from './client';
import { setToken, setRefreshToken, setUser } from '../utils/auth';

export const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password });
  const payload = res.data !== undefined ? res.data : res;
  if (payload.accessToken) setToken(payload.accessToken);
  if (payload.refreshToken) setRefreshToken(payload.refreshToken);
  if (payload.user) setUser(payload.user);
  return payload;
};

export const register = async (data) => {
  const res = await api.post('/auth/register', data);
  const payload = res.data !== undefined ? res.data : res;
  if (payload.accessToken) setToken(payload.accessToken);
  if (payload.refreshToken) setRefreshToken(payload.refreshToken);
  if (payload.user) setUser(payload.user);
  return payload;
};

export const getProfile = async () => {
  const res = await api.get('/auth/me');
  return res.data !== undefined ? res.data : res;
};

export const updateProfile = async (data) => {
  const res = await api.put('/auth/me', data);
  return res.data !== undefined ? res.data : res;
};

export const forgotPassword = async (email) => {
  const res = await api.post('/auth/forgot-password', { email });
  return res.data !== undefined ? res.data : res;
};

export const resetPassword = async (email, otp, newPassword) => {
  const res = await api.post('/auth/reset-password', { email, otp, newPassword });
  return res.data !== undefined ? res.data : res;
};
