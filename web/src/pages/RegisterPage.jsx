import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../api/auth';
import './Auth.css';

const ROLES = [
  { label: 'Doctor (MDS / OMFS)',  value: 'ROLE_DOCTOR'   },
  { label: 'Student (BDS / MDS)',  value: 'ROLE_STUDENT'  },
];

const DEPARTMENTS = [
  'Oral and Maxillofacial Surgery',
  'Oral Medicine and Radiology',
  'Orthodontics and Dentofacial Orthopaedics',
  'Periodontology',
  'Prosthodontics',
  'Conservative Dentistry and Endodontics',
  'Pedodontics',
  'Oral Pathology and Microbiology',
];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ROLE_DOCTOR',
    licenseNumber: '',
    department: 'Oral and Maxillofacial Surgery',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm]   = useState(false);
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
        fullName:      form.fullName,
        username:      form.username,
        email:         form.email,
        password:      form.password,
        role:          form.role,
        licenseNumber: form.licenseNumber,
        department:    form.department,
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
      <div className="auth-orb auth-orb-1" />
      <div className="auth-orb auth-orb-2" />
      <div className="auth-orb auth-orb-3" />
      <div className="auth-bg-grid" />

      <div className="auth-card glass-card" style={{ maxWidth: 540 }}>
        <div className="auth-header">
          <div className="auth-logo">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
          </div>
          <h1 className="auth-brand-name">Neo OMFS AI</h1>
          <div className="auth-brand-sub">CLINICAL SUPPORT SYSTEM</div>
          <div className="auth-badge">SIMATS &bull; Dept. of Oral &amp; Maxillofacial Surgery</div>
        </div>

        <h2 className="auth-title">Create Account</h2>
        <p className="auth-sub">Register with your institutional credentials</p>

        {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form" autoComplete="off">
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="fullName">Full Name *</label>
              <input id="fullName" data-testid="register-fullName" className="form-input glass-input" name="fullName" placeholder="Dr. Sujith Kumar" value={form.fullName} onChange={handle} required />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username *</label>
              <input id="username" data-testid="register-username" className="form-input glass-input" name="username" placeholder="sujith.kumar" value={form.username} onChange={handle} required minLength={3} />
            </div>
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="role">Role *</label>
              <select id="role" data-testid="register-role" className="form-input glass-input form-select" name="role" value={form.role} onChange={handle}>
                {ROLES.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="licenseNumber">License / Reg. No.</label>
              <input id="licenseNumber" data-testid="register-licenseNumber" className="form-input glass-input" name="licenseNumber" placeholder="2021BDS0042" value={form.licenseNumber} onChange={handle} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Institutional Email *</label>
            <input id="email" data-testid="register-email" className="form-input glass-input" type="email" name="email" placeholder="you@simats.ac.in" value={form.email} onChange={handle} required />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="department">Department</label>
            <select id="department" data-testid="register-department" className="form-input glass-input form-select" name="department" value={form.department} onChange={handle}>
              {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="password">Password * (min 8 chars)</label>
              <div className="password-field-wrapper">
                <input id="password" data-testid="register-password" className="form-input glass-input" type={showPassword ? 'text' : 'password'} name="password" placeholder="••••••••" value={form.password} onChange={handle} required minLength={8} />
                <button type="button" className="password-toggle" onClick={() => setShowPassword(v => !v)} tabIndex={-1}>{showPassword ? '🙈' : '👁️'}</button>
              </div>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">Confirm Password *</label>
              <div className="password-field-wrapper">
                <input id="confirmPassword" data-testid="register-confirmPassword" className="form-input glass-input" type={showConfirm ? 'text' : 'password'} name="confirmPassword" placeholder="••••••••" value={form.confirmPassword} onChange={handle} required />
                <button type="button" className="password-toggle" onClick={() => setShowConfirm(v => !v)} tabIndex={-1}>{showConfirm ? '🙈' : '👁️'}</button>
              </div>
            </div>
          </div>

          <button id="register-submit" data-testid="register-submit" className="btn btn-primary auth-submit glass-btn" type="submit" disabled={loading}>
            {loading ? <><span className="spinner" /> Creating Account…</> : 'Create Account →'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
