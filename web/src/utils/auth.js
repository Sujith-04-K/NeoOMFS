// ── Auth utilities ────────────────────────────────────────────────
const TOKEN_KEY   = 'neoomfs_token';
const REFRESH_KEY = 'neoomfs_refresh';
const USER_KEY    = 'neoomfs_user';

export const getToken        = () => localStorage.getItem(TOKEN_KEY);
export const setToken        = (t) => localStorage.setItem(TOKEN_KEY, t);
export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY);
export const setRefreshToken = (t) => localStorage.setItem(REFRESH_KEY, t);

// Store full user profile from AuthResponse.user so we can read name + roles
export const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user));
export const getUser = () => {
  try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
  catch { return null; }
};

export const clearTokens = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
};

export const isAuthenticated = () => {
  const token = getToken();
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
};

// Backend JWT only contains sub (email); roles come from AuthResponse.user
export const getUserRole = () => {
  const user = getUser();
  if (!user) return 'STUDENT';
  const roles = user.roles || [];
  if (roles.includes('ROLE_ADMIN'))   return 'ADMIN';
  if (roles.includes('ROLE_FACULTY')) return 'FACULTY';
  if (roles.includes('ROLE_DOCTOR'))  return 'DOCTOR';
  return 'STUDENT';
};

export const getUserPayload = () => {
  const user = getUser();
  if (!user) return null;
  return { name: user.fullName, email: user.email, sub: user.email };
};

export const isFaculty = () => {
  const role = getUserRole();
  return role === 'FACULTY' || role === 'ADMIN';
};
