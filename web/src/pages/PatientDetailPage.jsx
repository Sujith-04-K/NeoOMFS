import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getPatient,
  getVitals,
  getMedicalHistory,
  getDental,
  getDecision,
  updateReviewStatus,
  evaluateDecision
} from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty } from '../utils/auth';
import './PatientDetail.css';

function InfoRow({ label, value, highlight = false }) {
  return (
    <div className="info-row" style={highlight ? { background: 'var(--card-hover)', padding: '8px 12px', borderRadius: '6px' } : undefined}>
      <span className="info-label">{label}</span>
      <span className="info-value" style={highlight ? { fontWeight: 700, color: 'var(--text-primary)' } : undefined}>
        {value || '—'}
      </span>
    </div>
  );
}

export default function PatientDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [vitals, setVitals] = useState(null);
  const [history, setHistory] = useState(null);
  const [dental, setDental] = useState(null);
  const [decision, setDecision] = useState(null);
  const [activeTab, setActiveTab] = useState('clinical');
  const [loading, setLoading] = useState(true);
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState(null);
  const faculty = isFaculty();

  const loadCaseData = async () => {
    setLoading(true);
    try {
      const [pData, vData, hData, dData, cData] = await Promise.all([
        getPatient(id),
        getVitals(id).catch(() => null),
        getMedicalHistory(id).catch(() => null),
        getDental(id).catch(() => null),
        getDecision(id).catch(() => null),
      ]);
      setPatient(pData);
      setVitals(vData);
      setHistory(hData);
      setDental(dData);
      setDecision(cData);
      if (pData?.reviewComments) {
        setComments(pData.reviewComments);
      }
    } catch (e) {
      console.error('Error loading patient case:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCaseData();
  }, [id]);

  const review = async (status) => {
    setSaving(true);
    setMsg(null);
    try {
      const updated = await updateReviewStatus(id, status, comments || undefined);
      setPatient(updated);
      setMsg({
        type: 'success',
        text: `Clinical case status updated to ${status.replace('_', ' ')}`,
      });
    } catch (e) {
      setMsg({ type: 'error', text: e.message || 'Failed to update review status' });
    } finally {
      setSaving(false);
    }
  };

  const runDecisionEvaluation = async () => {
    setSaving(true);
    setMsg(null);
    try {
      const evalRes = await evaluateDecision(id);
      setDecision(evalRes);
      setMsg({
        type: 'success',
        text: `Clinical AI fitness evaluation completed: ${evalRes?.fitnessStatus || 'Analyzed'}`,
      });
    } catch (e) {
      setMsg({ type: 'error', text: e.message || 'Failed to evaluate decision' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="loading" style={{ padding: '60px 0' }}>
        <span className="spinner" /> Syncing clinical assessment data…
      </div>
    );
  }

  if (!patient) {
    return (
      <div className="empty-state" style={{ padding: '60px 0' }}>
        <h3>Patient Case Not Found</h3>
        <p>The requested patient record (#{id}) does not exist in the shared database.</p>
        <button className="btn btn-primary" onClick={() => navigate('/patients')} style={{ marginTop: '16px' }}>
          &larr; Back to Registry
        </button>
      </div>
    );
  }

  const canReview = faculty && patient.assessmentStatus === 'PENDING_REVIEW';
  const canSubmit = !faculty && patient.assessmentStatus === 'DRAFT';
  const asaLevel = decision?.asaClassification || patient.asaClassification || 'ASA_II';
  const isHighRisk = asaLevel === 'ASA_III' || asaLevel === 'ASA_IV' || (vitals?.systolicBp >= 180);

  return (
    <div className="dashboard-container">
      {/* ── 1. Case Top Banner ── */}
      <div className="card" style={{ padding: '24px 28px', background: 'var(--card)' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <button className="btn btn-ghost btn-sm" data-testid="detail-back-btn" onClick={() => navigate(-1)}>
                &larr; Back
              </button>
              <code className="mrn-badge" style={{ fontSize: '0.9rem', padding: '4px 10px' }}>
                {patient.mrn || `#${patient.id}`}
              </code>
              <StatusBadge status={patient.assessmentStatus} />
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '0.78rem',
                  fontWeight: 800,
                  background: isHighRisk ? 'var(--red-bg)' : 'var(--blue-bg)',
                  color: isHighRisk ? 'var(--red)' : '#818cf8',
                  border: `1px solid ${isHighRisk ? 'rgba(239,68,68,0.4)' : 'rgba(129,140,248,0.4)'}`,
                }}
              >
                {isHighRisk && <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--red)' }} />}
                {asaLevel.replace('_', ' ')} RISK
              </span>
            </div>
            <h1 style={{ fontSize: '1.9rem', fontWeight: 800, color: 'var(--text-primary)' }}>
              {patient.fullName}
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Procedure: <strong style={{ color: 'var(--text-primary)' }}>{patient.procedureType || 'Not specified'}</strong> &bull; Registered by {patient.createdByName || 'Student'} on {patient.createdAt ? patient.createdAt.split(' ')[0] : 'Today'}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <button className="btn btn-ghost" data-testid="detail-sync-btn" onClick={loadCaseData} title="Sync DB">
              🔄 Sync DB
            </button>
            <button className="btn btn-primary" data-testid="detail-eval-btn" onClick={runDecisionEvaluation} disabled={saving}>
              ⚡ Run AI Clinical Assessment
            </button>
          </div>
        </div>
      </div>

      {/* ── 2. High Risk Clinical Alert Banner (if applicable) ── */}
      {isHighRisk && (
        <div className="priority-alert-card" style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.4)' }}>
          <div className="alert-card-header">
            <span className="alert-patient-name" style={{ color: 'var(--red)' }}>
              ⚠️ High-Risk Clinical Warning ({asaLevel.replace('_', ' ')})
            </span>
            <span className="alert-vitals">
              {vitals?.systolicBp ? `BP ${vitals.systolicBp}/${vitals.diastolicBp}` : 'SPECIALIST CLEARANCE REQUIRED'}
            </span>
          </div>
          <p className="alert-description" style={{ color: 'var(--text-primary)' }}>
            {vitals?.systolicBp >= 180
              ? 'Stage 2 Hypertension detected. Defer elective surgical intervention; urgent medical clearance and cardiac evaluation required.'
              : 'Patient risk classification ASA III / IV. Requires senior OMFS faculty clearance and anesthesia consult prior to surgery.'}
          </p>
        </div>
      )}

      {/* ── 3. Navigation Tabs ── */}
      <div style={{ display: 'flex', gap: '8px', borderBottom: '2px solid var(--border)', paddingBottom: '12px', flexWrap: 'wrap' }}>
        {[
          { id: 'clinical', label: '📊 Vitals & Risk Analysis' },
          { id: 'history', label: '📋 Medical History & Comorbidities' },
          { id: 'airway', label: '🦷 Airway & Dental Exam' },
          { id: 'review', label: '⚖️ Faculty Review & Clearance' },
        ].map(tab => (
          <button
            key={tab.id}
            type="button"
            data-testid={`detail-tab-${tab.id}`}
            onClick={() => setActiveTab(tab.id)}
            style={{
              padding: '10px 20px',
              borderRadius: 'var(--radius-sm)',
              fontWeight: 700,
              fontSize: '0.9rem',
              background: activeTab === tab.id ? 'var(--accent)' : 'transparent',
              color: activeTab === tab.id ? '#fff' : 'var(--text-secondary)',
              border: 'none',
              cursor: 'pointer',
              transition: 'var(--transition)',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* ── 4. Tab Content ── */}
      <div className="detail-grid" style={{ marginTop: 0 }}>
        {/* LEFT COLUMN (70% width on large screens) */}
        <div className="detail-left" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {activeTab === 'clinical' && (
            <>
              <div className="card">
                <div className="section-title">Patient Demographics & Contact</div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <InfoRow label="Date of Birth" value={patient.dateOfBirth} />
                  <InfoRow label="Age / Gender" value={`${patient.age || '—'} years • ${patient.gender || '—'}`} />
                  <InfoRow label="Blood Group" value={patient.bloodGroup} />
                  <InfoRow label="Phone Number" value={patient.phoneNumber} />
                  <InfoRow label="Address" value={patient.address} />
                  <InfoRow label="Emergency Contact" value={`${patient.emergencyContact || '—'} (${patient.emergencyPhone || '—'})`} />
                </div>
              </div>

              <div className="card">
                <div className="section-title">Vitals & Physiological Assessment</div>
                {vitals ? (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
                    <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: 'var(--radius-sm)' }}>
                      <div className="info-label">Blood Pressure</div>
                      <div style={{ fontSize: '1.4rem', fontWeight: 800, color: vitals.systolicBp >= 140 ? 'var(--red)' : 'var(--text-primary)' }}>
                        {vitals.systolicBp || '—'}/{vitals.diastolicBp || '—'} mmHg
                      </div>
                    </div>
                    <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: 'var(--radius-sm)' }}>
                      <div className="info-label">Heart Rate</div>
                      <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                        {vitals.heartRate || '—'} bpm
                      </div>
                    </div>
                    <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: 'var(--radius-sm)' }}>
                      <div className="info-label">Oxygen Saturation (SpO2)</div>
                      <div style={{ fontSize: '1.4rem', fontWeight: 800, color: vitals.spo2 < 95 ? 'var(--amber)' : 'var(--green)' }}>
                        {vitals.spo2 || '—'}%
                      </div>
                    </div>
                    <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: 'var(--radius-sm)' }}>
                      <div className="info-label">Random Blood Sugar (RBS)</div>
                      <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                        {vitals.rbs || '—'} mg/dL
                      </div>
                    </div>
                  </div>
                ) : (
                  <p style={{ color: 'var(--text-secondary)', padding: '16px 0' }}>
                    No vitals recorded yet. Use the assessment form to add BP, HR, SpO2, and glucose levels.
                  </p>
                )}
              </div>
            </>
          )}

          {activeTab === 'history' && (
            <div className="card">
              <div className="section-title">Medical History & Systemic Comorbidities</div>
              {history ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <InfoRow label="Hypertension Status" value={history.hypertension ? 'YES (Diagnosed)' : 'No'} highlight={history.hypertension} />
                  <InfoRow label="Diabetes Mellitus" value={history.diabetes ? 'YES (Diagnosed)' : 'No'} highlight={history.diabetes} />
                  <InfoRow label="Cardiac Disease" value={history.cardiacDisease ? 'YES (Diagnosed)' : 'No'} highlight={history.cardiacDisease} />
                  <InfoRow label="Asthma / Respiratory" value={history.asthma ? 'YES (Diagnosed)' : 'No'} />
                  <InfoRow label="Bleeding Disorder" value={history.bleedingDisorder ? 'YES' : 'No'} highlight={history.bleedingDisorder} />
                  <InfoRow label="Allergies / Drug Sensitivities" value={history.allergies || 'None reported'} />
                  <InfoRow label="Current Medications" value={history.currentMedications || 'None'} />
                  <InfoRow label="Prior Surgical History" value={history.surgicalHistory || 'No significant prior surgeries'} />
                </div>
              ) : (
                <p style={{ color: 'var(--text-secondary)', padding: '16px 0' }}>
                  No medical history comorbidities recorded for this patient case yet.
                </p>
              )}
            </div>
          )}

          {activeTab === 'airway' && (
            <div className="card">
              <div className="section-title">Airway & Dental Examination</div>
              {dental ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <InfoRow label="Mallampati Score" value={dental.mallampatiClass || 'Class I'} highlight />
                  <InfoRow label="Mouth Opening (mm)" value={dental.mouthOpeningMm ? `${dental.mouthOpeningMm} mm` : '—'} />
                  <InfoRow label="Thyromental Distance" value={dental.thyromentalDistance ? `${dental.thyromentalDistance} cm` : '—'} />
                  <InfoRow label="Dentition Status" value={dental.dentitionStatus || 'Adequate'} />
                  <InfoRow label="Loose or Carious Teeth" value={dental.looseTeeth ? 'Yes — Protect airway' : 'No'} />
                </div>
              ) : (
                <p style={{ color: 'var(--text-secondary)', padding: '16px 0' }}>
                  No airway or dental examination parameters recorded yet.
                </p>
              )}
            </div>
          )}

          {activeTab === 'review' && (
            <div className="card">
              <div className="section-title">Faculty Clinical Clearance Record</div>
              <InfoRow label="Current Status" value={patient.assessmentStatus.replace('_', ' ')} highlight />
              <InfoRow label="Submitted By" value={patient.createdByName || 'Student'} />
              <InfoRow label="Reviewed By" value={patient.reviewedByName || 'Pending Faculty Review'} />
              {patient.approvedAt && <InfoRow label="Clearance Timestamp" value={patient.approvedAt} />}
              <div style={{ marginTop: '16px' }}>
                <div className="info-label" style={{ marginBottom: '8px' }}>Faculty Review Comments & Instructions</div>
                <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: 'var(--radius-sm)', minHeight: '60px' }}>
                  {patient.reviewComments || 'No clinical remarks recorded yet.'}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: Action & Workflow Panel */}
        <div className="detail-right" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Assessment Workflow Timeline */}
          <div className="card">
            <div className="section-title">Assessment Workflow</div>
            <div className="timeline">
              {[
                { s: 'DRAFT', label: 'Assessment Started' },
                { s: 'PENDING_REVIEW', label: 'Submitted for Review' },
                { s: 'APPROVED', label: 'Faculty Approved' },
                { s: 'NEEDS_REVISION', label: 'Revision Requested' },
              ].map(({ s, label }) => {
                const isActive = patient.assessmentStatus === s;
                const isDone =
                  !isActive &&
                  ((patient.assessmentStatus === 'APPROVED' && (s === 'DRAFT' || s === 'PENDING_REVIEW')) ||
                    (patient.assessmentStatus === 'NEEDS_REVISION' && (s === 'DRAFT' || s === 'PENDING_REVIEW')) ||
                    (patient.assessmentStatus === 'PENDING_REVIEW' && s === 'DRAFT'));
                return (
                  <div key={s} className={`timeline-step ${isActive ? 'active' : ''} ${isDone ? 'done' : ''}`}>
                    <div className="timeline-dot" />
                    <div>
                      <div className="timeline-label">{label}</div>
                      {isActive && <StatusBadge status={s} />}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Student Action: Submit for Review */}
          {canSubmit && (
            <div className="card action-card">
              <div className="section-title">Submit for Review</div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: 14 }}>
                Once all preoperative assessment steps are verified, submit this case for faculty sign-off.
              </p>
              {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 12 }}>{msg.text}</div>}
              <button
                className="btn btn-primary"
                data-testid="detail-submit-review-btn"
                style={{ width: '100%', padding: '12px' }}
                disabled={saving}
                onClick={() => review('PENDING_REVIEW')}
              >
                {saving ? <><span className="spinner" /> Submitting…</> : '📤 Submit for Faculty Review'}
              </button>
            </div>
          )}

          {/* Faculty Review Action Panel */}
          {canReview && (
            <div className="card action-card" style={{ border: '1px solid var(--border-light)' }}>
              <div className="section-title">Faculty Clearance Actions</div>
              {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 12 }}>{msg.text}</div>}
              <div className="form-group" style={{ marginBottom: 16 }}>
                <label className="form-label" style={{ fontWeight: 700 }}>
                  Review Comments & Instructions (optional)
                </label>
                <textarea
                  className="form-input"
                  rows={4}
                  placeholder="Enter preoperative clearance remarks, specialist consult requests, or revision instructions..."
                  value={comments}
                  onChange={(e) => setComments(e.target.value)}
                  style={{ resize: 'vertical' }}
                />
              </div>
              <div style={{ display: 'flex', gap: '12px' }}>
                <button
                  className="btn btn-success"
                  data-testid="detail-approve-btn"
                  style={{ flex: 1, padding: '12px' }}
                  disabled={saving}
                  onClick={() => review('APPROVED')}
                >
                  {saving ? <span className="spinner" /> : '✅'} Approve Case
                </button>
                <button
                  className="btn btn-danger"
                  data-testid="detail-revise-btn"
                  style={{ flex: 1, padding: '12px' }}
                  disabled={saving}
                  onClick={() => review('NEEDS_REVISION')}
                >
                  {saving ? <span className="spinner" /> : '🔄'} Request Revision
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

