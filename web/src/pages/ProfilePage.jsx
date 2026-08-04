import React, { useEffect, useState } from 'react';
import { getProfile, updateProfile } from '../api/auth';
import { getUserRole } from '../utils/auth';

export default function ProfilePage() {
  const [profile,  setProfile]  = useState(null);
  const [form,     setForm]     = useState({});
  const [editing,  setEditing]  = useState(false);
  const [loading,  setLoading]  = useState(true);
  const [saving,   setSaving]   = useState(false);
  const [msg,      setMsg]      = useState(null);
  const role = getUserRole();

  useEffect(() => {
    getProfile()
      .then(p => {
        setProfile(p);
        // UpdateProfileRequest expects: fullName, username, licenseNumber, department, institution, phoneNumber
        setForm({
          fullName:      p.fullName || '',
          username:      p.username || '',
          licenseNumber: p.licenseNumber || '',
          department:    p.department || '',
          institution:   p.institution || '',
          phoneNumber:   p.phoneNumber || '',
        });
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMsg(null);
    try {
      const updated = await updateProfile(form);
      setProfile(updated);
      setEditing(false);
      setMsg({ type: 'success', text: 'Profile updated successfully.' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading"><span className="spinner" /> Loading profile…</div>;
  if (!profile) return <div className="empty-state"><h3>Unable to load profile</h3></div>;

  const roleColor = {
    ADMIN: 'var(--red)', FACULTY: 'var(--amber)',
    DOCTOR: 'var(--accent)', STUDENT: 'var(--green)',
  }[role] || 'var(--accent)';

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  return (
    <div style={{ maxWidth: 640 }}>
      <div className="page-header">
        <h1>My Profile</h1>
        <p>Manage your institutional account information</p>
      </div>

      {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 16 }}>{msg.text}</div>}

      {/* Avatar card */}
      <div className="card" style={{ display: 'flex', alignItems: 'center', gap: 20, marginBottom: 20 }}>
        <div style={{
          width: 72, height: 72, borderRadius: '50%',
          background: 'linear-gradient(135deg, var(--accent), var(--blue))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '1.8rem', fontWeight: 900, color: '#fff', flexShrink: 0
        }}>
          {(profile.fullName || profile.username || 'U').charAt(0).toUpperCase()}
        </div>
        <div>
          <div style={{ fontSize: '1.2rem', fontWeight: 700 }}>{profile.fullName}</div>
          <div style={{ color: 'var(--text-secondary)', fontSize: '.875rem' }}>{profile.email}</div>
          <span style={{
            display: 'inline-block', marginTop: 6,
            padding: '2px 10px', borderRadius: 999,
            background: `${roleColor}20`, color: roleColor,
            fontSize: '.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.05em'
          }}>{role}</span>
        </div>
      </div>

      {/* Details */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <div className="section-title">Account Details</div>
          {!editing && (
            <button className="btn btn-ghost btn-sm" onClick={() => setEditing(true)}>Edit Profile</button>
          )}
        </div>

        {editing ? (
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div className="form-group">
                <label className="form-label">Full Name *</label>
                <input className="form-input" name="fullName" value={form.fullName} onChange={handle} required />
              </div>
              <div className="form-group">
                <label className="form-label">Username *</label>
                <input className="form-input" name="username" value={form.username} onChange={handle} required minLength={3} />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div className="form-group">
                <label className="form-label">License / Reg. No.</label>
                <input className="form-input" name="licenseNumber" value={form.licenseNumber} onChange={handle} placeholder="2021BDS0042" />
              </div>
              <div className="form-group">
                <label className="form-label">Phone</label>
                <input className="form-input" name="phoneNumber" value={form.phoneNumber} onChange={handle} placeholder="+91-XXXXX-XXXXX" />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Department</label>
              <input className="form-input" name="department" value={form.department} onChange={handle} placeholder="Oral and Maxillofacial Surgery" />
            </div>
            <div className="form-group">
              <label className="form-label">Institution</label>
              <input className="form-input" name="institution" value={form.institution} onChange={handle} placeholder="SIMATS" />
            </div>
            <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setEditing(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
                {saving ? <><span className="spinner" style={{ width: 14, height: 14 }} /> Saving…</> : 'Save Changes'}
              </button>
            </div>
          </form>
        ) : (
          <div>
            {[
              ['Full Name',      profile.fullName],
              ['Username',       profile.username],
              ['Email',          profile.email],
              ['Role',           role],
              ['License / Reg.', profile.licenseNumber || '—'],
              ['Department',     profile.department || '—'],
              ['Institution',    profile.institution || '—'],
              ['Phone',          profile.phoneNumber || '—'],
            ].map(([label, value]) => (
              <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '9px 0', borderBottom: '1px solid var(--border)' }}>
                <span style={{ fontSize: '.75rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '.05em', color: 'var(--text-muted)' }}>{label}</span>
                <span style={{ fontSize: '.875rem', color: 'var(--text-primary)' }}>{value}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
