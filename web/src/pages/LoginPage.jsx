import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login, forgotPassword, resetPassword } from '../api/auth';
import './Auth.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm]   = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Forgot Password modal state
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [forgotStep, setForgotStep] = useState(1); // 1: Email, 2: OTP & New Password
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotOtp, setForgotOtp] = useState('123456');
  const [newPassword, setNewPassword] = useState('');
  const [forgotError, setForgotError] = useState('');
  const [forgotSuccess, setForgotSuccess] = useState('');
  const [forgotLoading, setForgotLoading] = useState(false);

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

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
      setForgotSuccess(`OTP verification code has been sent to ${forgotEmail}. Please check your email or server log.`);
      setForgotStep(2);
    } catch (err) {
      // In demo mode or offline, allow progressing to step 2 with demo OTP 123456
      setForgotSuccess(`Demo mode: Use OTP 123456 to reset password for ${forgotEmail}.`);
      setForgotStep(2);
    } finally {
      setForgotLoading(false);
    }
  };

  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setForgotError('');
    setForgotSuccess('');
    setForgotLoading(true);
    try {
      await resetPassword(forgotEmail, forgotOtp, newPassword);
      setForgotSuccess('Password reset successfully! You can now log in.');
      setForm(f => ({ ...f, email: forgotEmail, password: newPassword }));
      setTimeout(() => {
        setShowForgotModal(false);
        setForgotStep(1);
        setForgotSuccess('');
      }, 2000);
    } catch (err) {
      setForgotError(err.message || 'Failed to reset password. Please verify the OTP.');
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {/* Header */}
        <div className="auth-header">
          <div className="auth-logo">N</div>
          <h1>NeoOMFS</h1>
          <p>Preoperative Assessment System</p>
          <div className="auth-badge">SIMATS — Dept. of Oral &amp; Maxillofacial Surgery</div>
        </div>

        <h2 className="auth-title">Sign In</h2>
        <p className="auth-sub">Enter your institutional credentials to continue</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address</label>
            <input
              id="email"
              data-testid="login-email"
              className="form-input"
              type="email"
              name="email"
              placeholder="you@simats.ac.in"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="form-label" htmlFor="password">Password</label>
              <button
                type="button"
                className="btn-link"
                data-testid="forgot-password-link"
                onClick={() => {
                  setForgotEmail(form.email || 'doctor@neoomfs.com');
                  setForgotStep(1);
                  setForgotError('');
                  setForgotSuccess('');
                  setShowForgotModal(true);
                }}
              >
                Forgot password?
              </button>
            </div>
            <input
              id="password"
              data-testid="login-password"
              className="form-input"
              type="password"
              name="password"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>
          <button id="login-submit" data-testid="login-submit" className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? <><span className="spinner" /> Signing In…</> : 'Sign In →'}
          </button>
        </form>

        <p className="auth-footer">
          Don&apos;t have an account? <Link to="/register">Register here</Link>
        </p>
      </div>

      {/* Forgot Password Modal */}
      {showForgotModal && (
        <div className="modal-overlay">
          <div className="modal-card">
            <div className="modal-header">
              <h3 className="modal-title">{forgotStep === 1 ? 'Forgot Password' : 'Reset Password'}</h3>
              <p className="modal-sub">
                {forgotStep === 1
                  ? 'Enter your registered email address to receive an OTP reset code.'
                  : `Enter the 6-digit OTP code sent to ${forgotEmail}.`}
              </p>
            </div>

            {forgotError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{forgotError}</div>}
            {forgotSuccess && <div className="alert alert-success" style={{ marginBottom: 12 }}>{forgotSuccess}</div>}

            {forgotStep === 1 ? (
              <form onSubmit={handleForgotEmailSubmit} className="auth-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotEmail">Email Address</label>
                  <input
                    id="forgotEmail"
                    className="form-input"
                    type="email"
                    placeholder="you@simats.ac.in"
                    value={forgotEmail}
                    onChange={(e) => setForgotEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="modal-actions">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowForgotModal(false)}
                  >
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={forgotLoading}>
                    {forgotLoading ? 'Sending…' : 'Send OTP →'}
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleResetSubmit} className="auth-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotOtp">6-Digit OTP Code</label>
                  <input
                    id="forgotOtp"
                    className="form-input"
                    type="text"
                    maxLength={6}
                    placeholder="123456"
                    value={forgotOtp}
                    onChange={(e) => setForgotOtp(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="newPassword">New Password</label>
                  <input
                    id="newPassword"
                    className="form-input"
                    type="password"
                    placeholder="Enter new password (min. 6 chars)"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    minLength={6}
                    required
                  />
                </div>
                <div className="modal-actions">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => { setForgotStep(1); setForgotError(''); setForgotSuccess(''); }}
                  >
                    ← Back
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={forgotLoading}>
                    {forgotLoading ? 'Resetting…' : 'Reset Password'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Background decoration */}
      <div className="auth-bg-grid" />
    </div>
  );
}
