import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login, forgotPassword, resetPassword } from '../api/auth';
import './Auth.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Forgot Password modal state
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [forgotStep, setForgotStep] = useState(1); // 1: Email, 2: OTP & New Password
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotOtp, setForgotOtp] = useState('');         // ← starts EMPTY (user must enter real OTP)
  const [newPassword, setNewPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [forgotError, setForgotError] = useState('');
  const [forgotSuccess, setForgotSuccess] = useState('');
  const [forgotLoading, setForgotLoading] = useState(false);

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotEmailSubmit = async (e) => {
    e.preventDefault();
    setForgotError('');
    setForgotSuccess('');
    setForgotLoading(true);
    try {
      await forgotPassword(forgotEmail);
      setForgotSuccess(`OTP sent to ${forgotEmail}. Check your email inbox (or server logs in dev mode).`);
      setForgotStep(2);
    } catch (err) {
      // Even if request fails (dev/email config), move to OTP step with guidance
      setForgotSuccess(`OTP request sent. In dev mode, check the Spring Boot console log for the OTP code sent to ${forgotEmail}.`);
      setForgotStep(2);
    } finally {
      setForgotLoading(false);
    }
  };

  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setForgotError('');
    setForgotSuccess('');
    if (forgotOtp.length < 4) {
      setForgotError('Please enter the full OTP code received via email.');
      return;
    }
    if (newPassword.length < 6) {
      setForgotError('New password must be at least 6 characters.');
      return;
    }
    setForgotLoading(true);
    try {
      await resetPassword(forgotEmail, forgotOtp, newPassword);
      setForgotSuccess('Password reset successfully! You can now sign in with your new password.');
      setForm((f) => ({ ...f, email: forgotEmail, password: newPassword }));
      setTimeout(() => {
        setShowForgotModal(false);
        setForgotStep(1);
        setForgotOtp('');
        setNewPassword('');
        setForgotSuccess('');
      }, 2500);
    } catch (err) {
      setForgotError(err.message || 'Failed to reset password. Please verify the OTP and try again.');
    } finally {
      setForgotLoading(false);
    }
  };

  const fillDemoAccount = (email) => {
    setForm({ email, password: 'Password@123' });
    setError('');
  };

  const openForgotModal = () => {
    setForgotEmail(form.email || '');
    setForgotStep(1);
    setForgotOtp('');
    setNewPassword('');
    setForgotError('');
    setForgotSuccess('');
    setShowForgotModal(true);
  };

  return (
    <div className="auth-page">
      {/* Ambient animated background orbs */}
      <div className="auth-orb auth-orb-1" />
      <div className="auth-orb auth-orb-2" />
      <div className="auth-orb auth-orb-3" />
      <div className="auth-bg-grid" />

      <div className="auth-card glass-card">
        {/* Header */}
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

        <h2 className="auth-title">Sign In</h2>
        <p className="auth-sub">Enter your institutional credentials to access the system</p>


        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form" autoComplete="off">
          <div className="form-group">
            <label className="form-label" htmlFor="email">Institutional Email</label>
            <input
              id="email"
              data-testid="login-email"
              className="form-input glass-input"
              type="email"
              name="email"
              placeholder="you@simats.ac.in"
              value={form.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </div>
          <div className="form-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="form-label" htmlFor="password">Password</label>
              <button type="button" className="btn-link" data-testid="forgot-password-link" onClick={openForgotModal}>
                Forgot password?
              </button>
            </div>
            <div className="password-field-wrapper">
              <input
                id="password"
                data-testid="login-password"
                className="form-input glass-input"
                type={showPassword ? 'text' : 'password'}
                name="password"
                placeholder="••••••••"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword((v) => !v)}
                tabIndex={-1}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>
          <button id="login-submit" data-testid="login-submit" className="btn btn-primary auth-submit glass-btn" type="submit" disabled={loading}>
            {loading ? <><span className="spinner" /> Signing In…</> : 'Sign In →'}
          </button>
        </form>

        <p className="auth-footer">
          Don&apos;t have an institutional account? <Link to="/register">Register new account</Link>
        </p>
      </div>

      {/* Forgot Password Modal */}
      {showForgotModal && (
        <div className="modal-overlay">
          <div className="modal-card glass-card">
            <div className="modal-header">
              <h3 className="modal-title">
                {forgotStep === 1 ? '🔐 Forgot Password' : '✉️ Enter OTP & New Password'}
              </h3>
              <p className="modal-sub">
                {forgotStep === 1
                  ? 'Enter your registered institutional email to receive a one-time password (OTP).'
                  : `Enter the OTP sent to ${forgotEmail} and set your new password.`}
              </p>
            </div>

            {forgotError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{forgotError}</div>}
            {forgotSuccess && <div className="alert alert-success" style={{ marginBottom: 12 }}>{forgotSuccess}</div>}

            {forgotStep === 1 ? (
              <form onSubmit={handleForgotEmailSubmit} className="auth-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotEmail">Institutional Email Address</label>
                  <input
                    id="forgotEmail"
                    data-testid="forgot-email-input"
                    className="form-input glass-input"
                    type="email"
                    placeholder="you@simats.ac.in"
                    value={forgotEmail}
                    onChange={(e) => setForgotEmail(e.target.value)}
                    required
                    autoFocus
                  />
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowForgotModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" data-testid="forgot-send-otp-btn" className="btn btn-primary glass-btn" disabled={forgotLoading}>
                    {forgotLoading ? 'Sending…' : 'Send OTP →'}
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleResetSubmit} className="auth-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotOtp">OTP Code (from email / server log)</label>
                  <input
                    id="forgotOtp"
                    data-testid="forgot-otp-input"
                    className="form-input glass-input otp-input"
                    type="text"
                    inputMode="numeric"
                    maxLength={8}
                    placeholder="Enter OTP code"
                    value={forgotOtp}
                    onChange={(e) => setForgotOtp(e.target.value.replace(/\D/g, ''))}
                    required
                    autoFocus
                    style={{ letterSpacing: '0.3em', fontSize: '1.4rem', textAlign: 'center', fontWeight: 800 }}
                  />
                  <span className="form-hint">Check your email inbox or Spring Boot console log for the OTP</span>
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="newPassword">New Password</label>
                  <div className="password-field-wrapper">
                    <input
                      id="newPassword"
                      data-testid="forgot-new-password-input"
                      className="form-input glass-input"
                      type={showNewPassword ? 'text' : 'password'}
                      placeholder="Enter new password (min. 8 chars)"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      minLength={6}
                      required
                    />
                    <button
                      type="button"
                      className="password-toggle"
                      onClick={() => setShowNewPassword((v) => !v)}
                      tabIndex={-1}
                    >
                      {showNewPassword ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>
                <div className="modal-actions">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => { setForgotStep(1); setForgotError(''); setForgotSuccess(''); }}
                  >
                    ← Back
                  </button>
                  <button type="submit" data-testid="forgot-reset-btn" className="btn btn-primary glass-btn" disabled={forgotLoading}>
                    {forgotLoading ? 'Resetting…' : 'Reset Password'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
