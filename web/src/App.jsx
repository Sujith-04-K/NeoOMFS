import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { isAuthenticated } from './utils/auth';
import Navbar       from './components/Navbar';
import LoginPage    from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage          from './pages/DashboardPage';
import PatientLogPage         from './pages/PatientLogPage';
import PatientDetailPage      from './pages/PatientDetailPage';
import PatientRegistrationPage from './pages/PatientRegistrationPage';
import ProfilePage            from './pages/ProfilePage';

// ── Protected layout: sidebar + main content ──
function AppLayout() {
  if (!isAuthenticated()) return <Navigate to="/login" replace />;
  return (
    <div className="app-layout">
      <Navbar />
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected */}
        <Route element={<AppLayout />}>
          <Route index             element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/patients"                element={<PatientLogPage />} />
          <Route path="/patients/new"            element={<PatientRegistrationPage />} />
          <Route path="/patients/:id"            element={<PatientDetailPage />} />
          <Route path="/profile"                 element={<ProfilePage />} />
        </Route>

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
