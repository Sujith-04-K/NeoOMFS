import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { clearTokens, getUserPayload, getUserRole, isFaculty } from '../utils/auth';
import './Navbar.css';

const NavItem = ({ to, icon, label }) => (
  <NavLink to={to} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
    <span className="nav-icon">{icon}</span>
    <span className="nav-label">{label}</span>
  </NavLink>
);

const ROLE_LABELS = {
  ADMIN:   { label: 'Administrator', cls: 'admin' },
  FACULTY: { label: 'Faculty / HOD', cls: 'faculty' },
  DOCTOR:  { label: 'Doctor (MDS)',  cls: 'doctor' },
  STUDENT: { label: 'Student',       cls: 'student' },
};

export default function Navbar() {
  const navigate = useNavigate();
  const payload  = getUserPayload();
  const role     = getUserRole();           // 'ADMIN' | 'FACULTY' | 'DOCTOR' | 'STUDENT'
  const faculty  = isFaculty();
  const roleInfo = ROLE_LABELS[role] || ROLE_LABELS.STUDENT;

  const handleLogout = () => {
    clearTokens();
    navigate('/login');
  };

  const displayName = payload?.name || payload?.sub || 'User';

  return (
    <nav className="navbar">
      {/* Logo */}
      <div className="nav-logo">
        <div className="nav-logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
            <circle cx="12" cy="10" r="3" />
          </svg>
        </div>
        <div>
          <div className="nav-logo-title">Neo OMFS AI</div>
          <div className="nav-logo-sub">CLINICAL SUPPORT</div>
        </div>
      </div>

      {/* User chip */}
      <div className="nav-user">
        <div className="nav-avatar">{displayName.charAt(0).toUpperCase()}</div>
        <div>
          <div className="nav-user-name">{displayName}</div>
          <div className={`nav-role ${roleInfo.cls}`}>{roleInfo.label}</div>
        </div>
      </div>

      <div className="nav-divider" />

      {/* Navigation Links */}
      <div className="nav-links">
        <NavItem to="/dashboard"    icon="📊" label="Clinical Dashboard" />
        <NavItem to="/patients"     icon="📋" label="Patient Registry" />
        {!faculty && (
          <NavItem to="/patients/new" icon="➕" label="New Assessment" />
        )}
        <NavItem to="/profile"      icon="👤" label="My Profile" />
      </div>

      <div style={{ flex: 1 }} />

      <button className="nav-logout" onClick={handleLogout}>
        <span>⎋</span> Sign Out
      </button>
    </nav>
  );
}
