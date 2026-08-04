import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { clearTokens, getUserPayload, isFaculty } from '../utils/auth';
import './Navbar.css';

const NavItem = ({ to, icon, label }) => (
  <NavLink
    to={to}
    className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
  >
    <span className="nav-icon">{icon}</span>
    <span className="nav-label">{label}</span>
  </NavLink>
);

export default function Navbar() {
  const navigate = useNavigate();
  const payload  = getUserPayload();
  const faculty  = isFaculty();

  const handleLogout = () => {
    clearTokens();
    navigate('/login');
  };

  const displayName = payload?.name || payload?.sub || 'User';
  const role = faculty ? 'Faculty' : 'Student';

  return (
    <nav className="navbar">
      {/* Logo */}
      <div className="nav-logo">
        <div className="nav-logo-icon">N</div>
        <div>
          <div className="nav-logo-title">NeoOMFS</div>
          <div className="nav-logo-sub">Clinical System</div>
        </div>
      </div>

      {/* User chip */}
      <div className="nav-user">
        <div className="nav-avatar">{displayName.charAt(0).toUpperCase()}</div>
        <div>
          <div className="nav-user-name">{displayName}</div>
          <div className={`nav-role ${faculty ? 'faculty' : 'student'}`}>{role}</div>
        </div>
      </div>

      <div className="nav-divider" />

      {/* Links */}
      <div className="nav-links">
        <NavItem to="/dashboard"  icon="◈" label="Dashboard"  />
        <NavItem to="/patients"   icon="⊞" label="Patients"   />
        {!faculty && (
          <NavItem to="/patients/new" icon="＋" label="New Patient" />
        )}
        <NavItem to="/profile"    icon="◉" label="Profile"    />
      </div>

      <div style={{ flex: 1 }} />

      <button className="nav-logout" onClick={handleLogout}>
        <span>⎋</span> Logout
      </button>
    </nav>
  );
}
