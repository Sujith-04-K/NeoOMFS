import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getPatient, updateReviewStatus } from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty } from '../utils/auth';
import './PatientDetail.css';

function InfoRow({ label, value }) {
  return (
    <div className="info-row">
      <span className="info-label">{label}</span>
      <span className="info-value">{value || '—'}</span>
    </div>
  );
}

export default function PatientDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient,  setPatient]  = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [comments, setComments] = useState('');
  const [saving,   setSaving]   = useState(false);
  const [msg,      setMsg]      = useState(null);
  const faculty = isFaculty();

  useEffect(() => {
    getPatient(id)
      .then(setPatient)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  const review = async (status) => {
    setSaving(true);
    setMsg(null);
    try {
      const updated = await updateReviewStatus(id, status, comments || undefined);
      setPatient(updated);
      setMsg({ type: 'success', text: `Status updated to ${status.replace('_', ' ')}` });
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading"><span className="spinner" /> Loading patient…</div>;
  if (!patient) return <div className="empty-state"><h3>Patient not found</h3></div>;

  const canReview = faculty && patient.assessmentStatus === 'PENDING_REVIEW';
  const canSubmit = !faculty && patient.assessmentStatus === 'DRAFT';

  return (
    <div>
      {/* Header */}
      <div className="detail-header">
        <button className="btn btn-ghost btn-sm" onClick={() => navigate(-1)}>← Back</button>
        <div>
          <h1 className="detail-name">{patient.fullName}</h1>
          <div className="detail-meta">
            <code>{patient.mrn || `#${patient.id}`}</code>
            <StatusBadge status={patient.assessmentStatus} />
          </div>
        </div>
      </div>

      <div className="detail-grid">
        {/* Left: Patient Info */}
        <div className="detail-left">
          <div className="card">
            <div className="section-title">Patient Information</div>
            <InfoRow label="Date of Birth"     value={patient.dateOfBirth} />
            <InfoRow label="Age"               value={patient.age ? `${patient.age} years` : null} />
            <InfoRow label="Gender"            value={patient.gender} />
            <InfoRow label="Blood Group"       value={patient.bloodGroup} />
            <InfoRow label="Phone"             value={patient.phoneNumber} />
            <InfoRow label="Address"           value={patient.address} />
            <InfoRow label="Emergency Contact" value={patient.emergencyContact} />
            <InfoRow label="Emergency Phone"   value={patient.emergencyPhone} />
          </div>

          <div className="card" style={{ marginTop: 16 }}>
            <div className="section-title">Clinical Information</div>
            <InfoRow label="Procedure Type"    value={patient.procedureType} />
            <InfoRow label="Referring Doctor"  value={patient.referringDoctor} />
            <InfoRow label="Registered By"     value={patient.createdByName} />
            <InfoRow label="Registered At"     value={patient.createdAt} />
          </div>
        </div>

        {/* Right: Review Panel */}
        <div className="detail-right">
          {/* Status Timeline */}
          <div className="card">
            <div className="section-title">Assessment Workflow</div>
            <div className="timeline">
              {[
                { s: 'DRAFT',          label: 'Assessment Started'     },
                { s: 'PENDING_REVIEW', label: 'Submitted for Review'   },
                { s: 'APPROVED',       label: 'Faculty Approved'       },
                { s: 'NEEDS_REVISION', label: 'Revision Requested'     },
              ].map(({ s, label }) => {
                const isActive = patient.assessmentStatus === s;
                const isDone   = !isActive && (
                  (patient.assessmentStatus === 'APPROVED'       && (s === 'DRAFT' || s === 'PENDING_REVIEW')) ||
                  (patient.assessmentStatus === 'NEEDS_REVISION' && (s === 'DRAFT' || s === 'PENDING_REVIEW')) ||
                  (patient.assessmentStatus === 'PENDING_REVIEW' && s === 'DRAFT')
                );
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

          {/* Faculty Review Card */}
          {patient.reviewedByName && (
            <div className="card" style={{ marginTop: 16 }}>
              <div className="section-title">Faculty Review</div>
              <InfoRow label="Reviewed By"  value={patient.reviewedByName} />
              {patient.approvedAt && <InfoRow label="Approved At"  value={patient.approvedAt} />}
              {patient.reviewComments && (
                <div className="review-comment-box">
                  <div className="info-label" style={{ marginBottom: 6 }}>Faculty Comments</div>
                  <p>{patient.reviewComments}</p>
                </div>
              )}
            </div>
          )}

          {/* Student: Submit for review */}
          {canSubmit && (
            <div className="card action-card" style={{ marginTop: 16 }}>
              <div className="section-title">Submit for Review</div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '.875rem', marginBottom: 14 }}>
                Once the 8-step assessment is complete, submit this case for faculty review.
              </p>
              {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 12 }}>{msg.text}</div>}
              <button
                className="btn btn-primary"
                style={{ width: '100%' }}
                disabled={saving}
                onClick={() => review('PENDING_REVIEW')}
              >
                {saving ? <><span className="spinner" /> Submitting…</> : '📤 Submit for Faculty Review'}
              </button>
            </div>
          )}

          {/* Faculty: Approve / Needs Revision */}
          {canReview && (
            <div className="card action-card" style={{ marginTop: 16 }}>
              <div className="section-title">Faculty Review Actions</div>
              {msg && <div className={`alert alert-${msg.type}`} style={{ marginBottom: 12 }}>{msg.text}</div>}
              <div className="form-group" style={{ marginBottom: 14 }}>
                <label className="form-label">Review Comments (optional)</label>
                <textarea
                  className="form-input"
                  rows={3}
                  placeholder="Enter clinical remarks or revision instructions…"
                  value={comments}
                  onChange={e => setComments(e.target.value)}
                  style={{ resize: 'vertical' }}
                />
              </div>
              <div style={{ display: 'flex', gap: 10 }}>
                <button
                  className="btn btn-success"
                  style={{ flex: 1 }}
                  disabled={saving}
                  onClick={() => review('APPROVED')}
                >
                  {saving ? <span className="spinner" /> : '✅'} Approve
                </button>
                <button
                  className="btn btn-danger"
                  style={{ flex: 1 }}
                  disabled={saving}
                  onClick={() => review('NEEDS_REVISION')}
                >
                  {saving ? <span className="spinner" /> : '🔄'} Needs Revision
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
