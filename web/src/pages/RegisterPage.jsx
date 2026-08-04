import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../api/auth';
import './Auth.css';

// Backend role values (without ROLE_ prefix in RegisterRequest)
const ROLES = [
  { label: 'Student',  value: 'ROLE_STUDENT'  },
  { label: 'Doctor',   value: 'ROLE_DOCTOR'   },
  { label: 'Faculty',  value: 'ROLE_FACULTY'  },
];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',           // ← backend expects fullName (NOT name)
    username: '',           // ← required by backend
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ROLE_STUDENT',   // ← backend expects ROLE_ prefix
    licenseNumber: '',
    department: '',
  });
  const [error,   setError]   = useState('');
  const [loading, setLoading] = useState(false);

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }
    setLoading(true);
    try {
      await register({
        fullName:     form.fullName,
        username:     form.username,
        email:        form.email,
        password:     form.password,
        role:         form.role,
        licenseNumber: form.licenseNumber,
        department:   form.department,
      });
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 520 }}>
        <div className="auth-header">
          <div className="auth-logo">N</div>
          <h1>NeoOMFS</h1>
          <p>Preoperative Assessment System</p>
          <div className="auth-badge">SIMATS — Dept. of Oral &amp; Maxillofacial Surgery</div>
        </div>

        <h2 className="auth-title">Create Account</h2>
        <p className="auth-sub">Register with your institutional credentials</p>

        {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="fullName">Full Name *</label>
              <input id="fullName" data-testid="register-fullName" className="form-input" name="fullName" placeholder="Dr. Sujith Kumar" value={form.fullName} onChange={handle} required />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username *</label>
              <input id="username" data-testid="register-username" className="form-input" name="username" placeholder="sujith.kumar" value={form.username} onChange={handle} required minLength={3} />
            </div>
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="role">Role *</label>
              <select id="role" data-testid="register-role" className="form-input form-select" name="role" value={form.role} onChange={handle}>
                {ROLES.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="licenseNumber">License / Reg. No.</label>
              <input id="licenseNumber" data-testid="register-licenseNumber" className="form-input" name="licenseNumber" placeholder="2021BDS0042" value={form.licenseNumber} onChange={handle} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Institutional Email *</label>
            <input id="email" data-testid="register-email" className="form-input" type="email" name="email" placeholder="you@simats.ac.in" value={form.email} onChange={handle} required />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="department">Department</label>
            <input id="department" data-testid="register-department" className="form-input" name="department" placeholder="Oral and Maxillofacial Surgery" value={form.department} onChange={handle} />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="password">Password * (min 8 chars)</label>
              <input id="password" data-testid="register-password" className="form-input" type="password" name="password" placeholder="••••••••" value={form.password} onChange={handle} required minLength={8} />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">Confirm Password *</label>
              <input id="confirmPassword" data-testid="register-confirmPassword" className="form-input" type="password" name="confirmPassword" placeholder="••••••••" value={form.confirmPassword} onChange={handle} required />
            </div>
          </div>

          <button id="register-submit" data-testid="register-submit" className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? <><span className="spinner" /> Creating Account…</> : 'Create Account →'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
      <div className="auth-bg-grid" />
    </div>
  );
}
