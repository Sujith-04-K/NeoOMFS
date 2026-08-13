import React, { useEffect, useState } from 'react';
import { getProfile, updateProfile } from '../api/auth';
import { getUserRole } from '../utils/auth';

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState(null);
  const role = getUserRole();

  useEffect(() => {
    getProfile()
      .then((p) => {
        setProfile(p);
        setForm({
          fullName: p.fullName || '',
          username: p.username || '',
          licenseNumber: p.licenseNumber || '',
          department: p.department || 'Oral & Maxillofacial Surgery',
          institution: p.institution || 'SIMATS Hospital',
          phoneNumber: p.phoneNumber || '',
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
      setMsg({ type: 'success', text: 'Institutional profile updated successfully.' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="loading" style={{ padding: '60px 0' }}>
        <span className="spinner" /> Loading institutional profile…
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="empty-state" style={{ padding: '60px 0' }}>
        <h3>Unable to load profile</h3>
      </div>
    );
  }

  const roleColor =
    {
      ADMIN: 'var(--red)',
      FACULTY: 'var(--amber)',
      DOCTOR: 'var(--accent)',
      STUDENT: 'var(--green)',
    }[role] || 'var(--accent)';

  const handle = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  return (
    <div className="dashboard-container" style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* ── Header ── */}
      <div className="dashboard-header-row" style={{ alignItems: 'center' }}>
        <div className="greeting-block">
          <h2>Institutional Profile</h2>
          <p className="greeting-sub">SIMATS &bull; Department of Oral &amp; Maxillofacial Surgery</p>
        </div>
      </div>

      {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 16 }}>{msg.text}</div>}

      {/* ── Avatar Card ── */}
      <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '20px', padding: '24px' }}>
        <div
          style={{
            width: 80,
            height: 80,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--accent), #38bdf8)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '2rem',
            fontWeight: 900,
            color: '#fff',
            flexShrink: 0,
            boxShadow: '0 4px 12px rgba(79,70,229,0.3)',
          }}
        >
          {(profile.fullName || profile.username || 'U').charAt(0).toUpperCase()}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            <h3 style={{ fontSize: '1.4rem', fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
              {profile.fullName}
            </h3>
            <span
              style={{
                padding: '4px 12px',
                borderRadius: '999px',
                background: `${roleColor}20`,
                color: roleColor,
                fontSize: '0.72rem',
                fontWeight: 800,
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                border: `1px solid ${roleColor}60`,
              }}
            >
              {role}
            </span>
          </div>
          <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>
            {profile.email} &bull; {profile.department || 'Oral and Maxillofacial Surgery'}
          </div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '2px' }}>
            Institution: {profile.institution || 'SIMATS'}
          </div>
        </div>
      </div>

      {/* ── Account Details Card ── */}
      <div className="card" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div className="section-title" style={{ margin: 0 }}>
            Clinical &amp; Institutional Details
          </div>
          {!editing && (
            <button className="btn btn-primary btn-sm" onClick={() => setEditing(true)}>
              ✏️ Edit Profile
            </button>
          )}
        </div>

        {editing ? (
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Full Name *</label>
                <input className="form-input" name="fullName" value={form.fullName} onChange={handle} required />
              </div>
              <div className="form-group">
                <label className="form-label">Username *</label>
                <input className="form-input" name="username" value={form.username} onChange={handle} required minLength={3} />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Medical License / Reg. No.</label>
                <input className="form-input" name="licenseNumber" value={form.licenseNumber} onChange={handle} placeholder="e.g. 2021BDS0042" />
              </div>
              <div className="form-group">
                <label className="form-label">Phone Number</label>
                <input className="form-input" name="phoneNumber" value={form.phoneNumber} onChange={handle} placeholder="+91-XXXXX-XXXXX" />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Department</label>
                <input className="form-input" name="department" value={form.department} onChange={handle} placeholder="Oral and Maxillofacial Surgery" />
              </div>
              <div className="form-group">
                <label className="form-label">Institution</label>
                <input className="form-input" name="institution" value={form.institution} onChange={handle} placeholder="SIMATS Hospital" />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
              <button type="button" className="btn btn-ghost" onClick={() => setEditing(false)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving} style={{ padding: '10px 24px' }}>
                {saving ? (
                  <>
                    <span className="spinner" style={{ width: 14, height: 14 }} /> Saving…
                  </>
                ) : (
                  'Save Profile Changes'
                )}
              </button>
            </div>
          </form>
        ) : (
          <div>
            {[
              ['Full Name', profile.fullName],
              ['Institutional Username', profile.username],
              ['Official Email', profile.email],
              ['Clinical Role', role],
              ['Medical Reg. / License No.', profile.licenseNumber || 'Not specified'],
              ['Department', profile.department || 'Oral and Maxillofacial Surgery'],
              ['Hospital / Institution', profile.institution || 'SIMATS Hospital'],
              ['Contact Number', profile.phoneNumber || 'Not specified'],
            ].map(([label, value]) => (
              <div
                key={label}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  padding: '12px 0',
                  borderBottom: '1px solid var(--border)',
                }}
              >
                <span
                  style={{
                    fontSize: '0.78rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    color: 'var(--text-muted)',
                  }}
                >
                  {label}
                </span>
                <span style={{ fontSize: '0.9rem', color: 'var(--text-primary)', fontWeight: 600 }}>{value}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

