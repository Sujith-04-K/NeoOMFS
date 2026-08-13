import { api } from './client';

// client.js now returns the inner ApiResponse.data directly

export const searchPatients = async ({ search, status, page = 0, size = 20 } = {}) => {
  const params = new URLSearchParams();
  if (search) params.set('search', search);
  if (status && status !== 'ALL') params.set('status', status);
  params.set('page', page);
  params.set('size', size);
  return await api.get(`/patients?${params}`);
};

export const getPatient = async (id) => {
  return await api.get(`/patients/${id}`);
};

export const createPatient = async (data) => {
  return await api.post('/patients', data);
};

export const updateReviewStatus = async (id, status, reviewComments) => {
  return await api.patch(`/patients/${id}/review-status`, { status, reviewComments });
};

export const getDashboard = async () => {
  return await api.get('/dashboard');
};

// Wizard Step APIs (Steps 2 to 8)
export const saveMedicalHistory = async (patientId, data) => {
  return await api.post(`/patients/${patientId}/medical-history`, data);
};

export const saveDental = async (patientId, data) => {
  return await api.post(`/patients/${patientId}/dental`, data);
};

export const saveVitals = async (patientId, data) => {
  return await api.post(`/patients/${patientId}/vitals`, data);
};

export const saveLaboratory = async (patientId, data) => {
  return await api.post(`/patients/${patientId}/laboratory`, data);
};

export const saveRadiology = async (patientId, data) => {
  return await api.post(`/patients/${patientId}/radiology`, data);
};

export const evaluateDecision = async (patientId) => {
  return await api.post(`/patients/${patientId}/decision/evaluate`);
};

export const generateReport = async (patientId) => {
  return await api.post(`/patients/${patientId}/report/generate`);
};

// GET APIs for clinical assessment details
export const getVitals = async (patientId) => {
  return await api.get(`/patients/${patientId}/vitals`);
};

export const getMedicalHistory = async (patientId) => {
  return await api.get(`/patients/${patientId}/medical-history`);
};

export const getDental = async (patientId) => {
  return await api.get(`/patients/${patientId}/dental`);
};

export const getDecision = async (patientId) => {
  return await api.get(`/patients/${patientId}/decision`);
};

export const uploadRadiologyScan = async (file, folder = 'radiology') => {
  return await api.upload('/files/upload', file, folder);
};
