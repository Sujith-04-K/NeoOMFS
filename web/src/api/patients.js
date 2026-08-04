import { api } from './client';

export const searchPatients = async ({ search, status, page = 0, size = 20 } = {}) => {
  const params = new URLSearchParams();
  if (search) params.set('search', search);
  if (status && status !== 'ALL') params.set('status', status);
  params.set('page', page);
  params.set('size', size);
  const res = await api.get(`/patients?${params}`);
  return res.data !== undefined ? res.data : res;
};

export const getPatient = async (id) => {
  const res = await api.get(`/patients/${id}`);
  return res.data !== undefined ? res.data : res;
};

export const createPatient = async (data) => {
  const res = await api.post('/patients', data);
  return res.data !== undefined ? res.data : res;
};

export const updateReviewStatus = async (id, status, reviewComments) => {
  const res = await api.patch(`/patients/${id}/review-status`, { status, reviewComments });
  return res.data !== undefined ? res.data : res;
};

export const getDashboard = async () => {
  const res = await api.get('/dashboard');
  return res.data !== undefined ? res.data : res;
};
