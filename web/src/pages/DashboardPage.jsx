import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboard, searchPatients } from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty } from '../utils/auth';
import './Dashboard.css';

function StatCard({ label, value, sub, color }) {
  return (
    <div className={`stat-card ${color}`}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value ?? '—'}</div>
      {sub && <div className="stat-sub">{sub}</div>}
    </div>
  );
}

export default function DashboardPage() {
  const [stats,    setStats]    = useState(null);
  const [patients, setPatients] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const faculty = isFaculty();

  useEffect(() => {
    const load = async () => {
      try {
        const [s, p] = await Promise.all([
          getDashboard(),
          searchPatients({ size: 8 }),
        ]);
        setStats(s);
        setPatients((p && (Array.isArray(p) ? p : p.content)) || []);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return (
    <div className="loading"><span className="spinner" /> Loading dashboard…</div>
  );

  const pending  = patients.filter(p => p.assessmentStatus === 'PENDING_REVIEW').length;
  const approved = patients.filter(p => p.assessmentStatus === 'APPROVED').length;
  const revision = patients.filter(p => p.assessmentStatus === 'NEEDS_REVISION').length;

  return (
    <div>
      <div className="page-header">
        <h1>
          {faculty ? '🏥 Faculty Dashboard' : '📋 Student Dashboard'}
        </h1>
        <p>
          {faculty
            ? 'Review and approve preoperative assessments submitted by students'
            : 'Track your patient assessments and submit for faculty review'}
        </p>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <StatCard label="Total Patients" value={stats?.totalPatients}      sub="All registered cases"         color="" />
        <StatCard label="Pending Review"  value={stats?.pendingClinicalDecision} sub="Awaiting faculty sign-off" color="amber" />
        <StatCard label="Approved"        value={approved}                  sub="Faculty approved"              color="green" />
        <StatCard label="Needs Revision"  value={revision}                  sub="Returned for correction"      color="red" />
      </div>

      {/* Faculty pending highlight */}
      {faculty && pending > 0 && (
        <div className="pending-alert">
          <span className="pending-alert-icon">⚠</span>
          <div>
            <strong>{pending} case{pending > 1 ? 's' : ''} pending your review.</strong>
            <span> Review and approve assessments submitted by students.</span>
          </div>
          <Link to="/patients?status=PENDING_REVIEW" className="btn btn-warning btn-sm">
            Review Now
          </Link>
        </div>
      )}

      {/* Recent patients table */}
      <div className="card">
        <div className="dashboard-table-header">
          <div className="section-title">Recent Patients</div>
          <Link to="/patients" className="btn btn-ghost btn-sm">View All →</Link>
        </div>

        {patients.length === 0 ? (
          <div className="empty-state">
            <h3>No patients yet</h3>
            <p>
              {!faculty && <Link to="/patients/new" className="btn btn-primary btn-sm" style={{ marginTop: 12 }}>Register First Patient</Link>}
            </p>
          </div>
        ) : (
          <div className="table-wrapper" style={{ marginTop: 14 }}>
            <table>
              <thead>
                <tr>
                  <th>MRN</th>
                  <th>Name</th>
                  <th>Procedure</th>
                  <th>Registered By</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {patients.map(p => (
                  <tr key={p.id}>
                    <td><code style={{ color: 'var(--text-secondary)', fontSize: '.8rem' }}>{p.mrn || `#${p.id}`}</code></td>
                    <td style={{ fontWeight: 600 }}>{p.fullName}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{p.procedureType || '—'}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{p.createdByName || '—'}</td>
                    <td><StatusBadge status={p.assessmentStatus} /></td>
                    <td>
                      <Link to={`/patients/${p.id}`} className="btn btn-ghost btn-sm">View →</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
