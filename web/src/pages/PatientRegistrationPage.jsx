import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPatient } from '../api/patients';

const BLOOD_GROUPS = ['A+','A-','B+','B-','AB+','AB-','O+','O-'];
const GENDERS      = ['Male','Female','Other'];

// Calculate age from date of birth string
function calcAge(dob) {
  if (!dob) return null;
  const today = new Date();
  const birth  = new Date(dob);
  let age = today.getFullYear() - birth.getFullYear();
  const m = today.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
  return age > 0 ? age : null;
}

export default function PatientRegistrationPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '', dateOfBirth: '', gender: 'Male', bloodGroup: 'O+',
    phoneNumber: '', address: '',
    emergencyContact: '', emergencyPhone: '',
    procedureType: '', referringDoctor: '',
  });
  const [error,   setError]   = useState('');
  const [loading, setLoading] = useState(false);

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const age = calcAge(form.dateOfBirth);
    if (!age) {
      setError('Please enter a valid date of birth');
      return;
    }

    setLoading(true);
    try {
      // Backend PatientRequest expects: fullName, age (Integer, required),
      // dateOfBirth (LocalDate = "yyyy-MM-dd"), gender, bloodGroup, phoneNumber,
      // address, emergencyContact, emergencyPhone, procedureType, referringDoctor
      const patient = await createPatient({
        fullName:         form.fullName,
        age:              age,           // ← required @NotNull
        dateOfBirth:      form.dateOfBirth, // already "yyyy-MM-dd" from <input type="date">
        gender:           form.gender,
        bloodGroup:       form.bloodGroup,
        phoneNumber:      form.phoneNumber || null,
        address:          form.address    || null,
        emergencyContact: form.emergencyContact || null,
        emergencyPhone:   form.emergencyPhone   || null,
        procedureType:    form.procedureType    || null,
        referringDoctor:  form.referringDoctor  || null,
      });
      navigate(`/patients/${patient.id}`);
    } catch (err) {
      setError(err.message || 'Failed to register patient. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1>Register New Patient</h1>
        <p>Complete all fields to begin the preoperative assessment workflow</p>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 20 }}>{error}</div>}

      <form onSubmit={handleSubmit}>
        {/* Personal Info */}
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title" style={{ marginBottom: 16 }}>Personal Information</div>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label" htmlFor="fullName">Full Name *</label>
              <input id="fullName" data-testid="patient-fullName" className="form-input" name="fullName" placeholder="Patient full name" value={form.fullName} onChange={handle} required />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="dateOfBirth">Date of Birth *</label>
              <input id="dateOfBirth" data-testid="patient-dob" className="form-input" type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handle} required max={new Date().toISOString().split('T')[0]} />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="gender">Gender *</label>
              <select id="gender" data-testid="patient-gender" className="form-input form-select" name="gender" value={form.gender} onChange={handle} required>
                {GENDERS.map(g => <option key={g}>{g}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="bloodGroup">Blood Group</label>
              <select id="bloodGroup" data-testid="patient-bloodGroup" className="form-input form-select" name="bloodGroup" value={form.bloodGroup} onChange={handle}>
                {BLOOD_GROUPS.map(b => <option key={b}>{b}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="phoneNumber">Phone Number</label>
              <input id="phoneNumber" data-testid="patient-phone" className="form-input" name="phoneNumber" placeholder="+91-XXXXX-XXXXX" value={form.phoneNumber} onChange={handle} />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="address">Address</label>
              <input id="address" data-testid="patient-address" className="form-input" name="address" placeholder="Street, City" value={form.address} onChange={handle} />
            </div>
          </div>
          {/* Age preview */}
          {form.dateOfBirth && (
            <div style={{ marginTop: 10, color: 'var(--text-muted)', fontSize: '.8rem' }}>
              Calculated Age: <strong style={{ color: 'var(--text-secondary)' }}>{calcAge(form.dateOfBirth) ?? '—'} years</strong>
            </div>
          )}
        </div>

        {/* Emergency Contact */}
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title" style={{ marginBottom: 16 }}>Emergency Contact</div>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Contact Name</label>
              <input className="form-input" name="emergencyContact" placeholder="Contact person name" value={form.emergencyContact} onChange={handle} />
            </div>
            <div className="form-group">
              <label className="form-label">Contact Phone</label>
              <input className="form-input" name="emergencyPhone" placeholder="+91-XXXXX-XXXXX" value={form.emergencyPhone} onChange={handle} />
            </div>
          </div>
        </div>

        {/* Clinical */}
        <div className="card" style={{ marginBottom: 24 }}>
          <div className="section-title" style={{ marginBottom: 16 }}>Clinical Information</div>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Procedure Type</label>
              <input className="form-input" name="procedureType" placeholder="e.g. Surgical Extraction, Biopsy" value={form.procedureType} onChange={handle} />
            </div>
            <div className="form-group">
              <label className="form-label">Referring Doctor</label>
              <input className="form-input" name="referringDoctor" placeholder="Dr. Name" value={form.referringDoctor} onChange={handle} />
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 12 }}>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>Cancel</button>
          <button id="patient-submit" data-testid="patient-submit" type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? <><span className="spinner" /> Registering…</> : '✓ Register Patient'}
          </button>
        </div>
      </form>
    </div>
  );
}
