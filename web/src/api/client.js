import { getToken, getRefreshToken, setToken, clearTokens } from '../utils/auth';

const BASE = '/api/v1';
let isRefreshing = false;
let refreshSubscribers = [];

function onRefreshed(token) {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
}

async function request(method, path, body, isRetry = false) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && !isRetry && !path.includes('/auth/')) {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearTokens();
      window.location.href = '/login';
      throw new Error('Session expired. Please log in again.');
    }

    if (isRefreshing) {
      return new Promise(resolve => {
        refreshSubscribers.push(() => {
          resolve(request(method, path, body, true));
        });
      });
    }

    isRefreshing = true;
    try {
      const refreshRes = await fetch(`${BASE}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });

      if (!refreshRes.ok) throw new Error('Refresh failed');
      const data = await refreshRes.json();
      const newToken = data.data?.accessToken || data.accessToken;
      if (!newToken) throw new Error('Invalid refresh payload');

      setToken(newToken);
      isRefreshing = false;
      onRefreshed(newToken);
      return request(method, path, body, true);
    } catch (e) {
      isRefreshing = false;
      clearTokens();
      window.location.href = '/login';
      throw new Error('Session expired. Please log in again.');
    }
  } else if (res.status === 401) {
    if (!isRetry && !path.includes('/auth/')) {
      clearTokens();
      window.location.href = '/login';
    }
    throw new Error('Session expired. Please log in again.');
  }

  // Parse JSON — backend always returns ApiResponse<T> wrapper
  let json = {};
  try { json = await res.json(); } catch (_) { /* empty body */ }

  // If backend returned ApiResponse wrapper, surface the inner payload
  // ApiResponse shape: { success, message, data }
  const isApiWrapper = json && typeof json === 'object' && ('success' in json || 'data' in json);

  if (!res.ok) {
    // Extract error message from ApiResponse or raw body
    const msg = isApiWrapper
      ? (json.message || `Request failed: ${res.status}`)
      : (json.message || JSON.stringify(json) || `Request failed: ${res.status}`);
    throw new Error(msg);
  }

  // Return the inner .data if it's an ApiResponse wrapper, otherwise return json directly
  return isApiWrapper ? json.data : json;
}

export const api = {
  get:    (path)         => request('GET',    path),
  post:   (path, body)   => request('POST',   path, body),
  put:    (path, body)   => request('PUT',    path, body),
  patch:  (path, body)   => request('PATCH',  path, body),
  delete: (path)         => request('DELETE', path),

  // Authenticated file download — injects Bearer token so the backend accepts the request
  download: async (path, filename) => {
    const token = getToken();
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${BASE}${path}`, { method: 'GET', headers });
    if (!res.ok) throw new Error(`Download failed: ${res.status}`);
    const blob = await res.blob();
    const url  = window.URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = filename || 'report.pdf';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  },

  // Multipart file upload — omits Content-Type so browser sets the boundary automatically
  upload: async (path, file, folder = 'radiology') => {
    const token = getToken();
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const form = new FormData();
    form.append('file', file);
    const res = await fetch(`${BASE}${path}?folder=${encodeURIComponent(folder)}`, {
      method: 'POST',
      headers,
      body: form,
    });
    let json = {};
    try { json = await res.json(); } catch (_) {}
    if (!res.ok) throw new Error(json.message || `Upload failed: ${res.status}`);
    const isApiWrapper = json && typeof json === 'object' && ('success' in json || 'data' in json);
    return isApiWrapper ? json.data : json;
  },
};
