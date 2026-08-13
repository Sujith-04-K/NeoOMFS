import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getDashboard, searchPatients } from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty, getUserPayload } from '../utils/auth';
import './Dashboard.css';

// SVG Icons for Quick Actions & UI
const Icons = {
  search: (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8"></circle>
      <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
    </svg>
  ),
  assessment: (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
      <polyline points="14 2 14 8 20 8"></polyline>
      <line x1="16" y1="13" x2="8" y2="13"></line>
      <line x1="16" y1="17" x2="8" y2="17"></line>
      <polyline points="10 9 9 9 8 9"></polyline>
    </svg>
  ),
  addPatient: (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
      <circle cx="8.5" cy="7" r="4"></circle>
      <line x1="20" y1="8" x2="20" y2="14"></line>
      <line x1="23" y1="11" x2="17" y2="11"></line>
    </svg>
  ),
  uploadRadio: (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
      <polyline points="17 8 12 3 7 8"></polyline>
      <line x1="12" y1="3" x2="12" y2="15"></line>
    </svg>
  ),
  report: (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
      <polyline points="14 2 14 8 20 8"></polyline>
      <line x1="12" y1="18" x2="12" y2="12"></line>
      <line x1="9" y1="15" x2="15" y2="15"></line>
    </svg>
  ),
  alert: (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
      <line x1="12" y1="9" x2="12" y2="13"></line>
      <line x1="12" y1="17" x2="12.01" y2="17"></line>
    </svg>
  ),
  refresh: (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="23 4 23 10 17 10"></polyline>
      <polyline points="1 20 1 14 7 14"></polyline>
      <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
    </svg>
  )
};

export default function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const payload = getUserPayload();
  const faculty = isFaculty();
  const displayName = payload?.name || payload?.sub || 'Dr. Sharma';

  // Format today's date dynamically (e.g. "Monday, October 23rd")
  const formatDate = () => {
    const d = new Date();
    const options = { weekday: 'long', month: 'long', day: 'numeric' };
    return d.toLocaleDateString('en-US', options);
  };

  const loadDashboardData = async (isManual = false) => {
    if (isManual) setRefreshing(true);
    try {
      const [s, p] = await Promise.all([
        getDashboard(),
        searchPatients({ size: 15 }),
      ]);
      setStats(s);
      setPatients((p && (Array.isArray(p) ? p : p.content)) || []);
    } catch (e) {
      console.error('Error fetching dashboard data:', e);
    } finally {
      setLoading(false);
      if (isManual) setRefreshing(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  // Filter patients by search term across MRN, Full Name, Procedure, ID, or Student ID/Creator
  const filteredPatients = patients.filter(p => {
    if (!searchTerm.trim()) return true;
    const q = searchTerm.toLowerCase();
    const mrn = (p.mrn || `#${p.id}`).toLowerCase();
    const name = (p.fullName || '').toLowerCase();
    const proc = (p.procedureType || '').toLowerCase();
    const creator = (p.createdByName || '').toLowerCase();
    const submitter = (p.submittedBy || '').toLowerCase();
    return mrn.includes(q) || name.includes(q) || proc.includes(q) || creator.includes(q) || submitter.includes(q);
  });

  const handleActionClick = (path) => {
    navigate(path);
  };

  if (loading) {
    return (
      <div className="loading">
        <span className="spinner" /> Syncing clinical database…
      </div>
    );
  }

  const totalCount = stats?.totalPatients ?? 0;
  const pendingCount = stats?.pendingClinicalDecision ?? 0;
  const completedCount = stats?.fitPatients ?? 0;
  const highRiskCount = stats?.highRiskPatients ?? 0;

  return (
    <div className="dashboard-container">
      {/* ── 1. Top Clinical Search Bar ── */}
      <div className="dashboard-search-row">
        <div className="dashboard-search-bar">
          <span className="search-icon">{Icons.search}</span>
          <input
            type="text"
            data-testid="dashboard-search-input"
            placeholder="Search Patient / Case ID / Lab Result..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button className="clear-search-btn" data-testid="dashboard-clear-search" onClick={() => setSearchTerm('')}>✕</button>
          )}
        </div>
        <button
          className={`refresh-btn ${refreshing ? 'spinning' : ''}`}
          data-testid="dashboard-refresh-btn"
          onClick={() => loadDashboardData(true)}
          title="Refresh Live Database"
        >
          {Icons.refresh} <span>Sync DB</span>
        </button>
      </div>

      {/* ── 2. Greeting Header & System Status ── */}
      <div className="dashboard-header-row">
        <div className="greeting-block">
          <h2>Good morning, {displayName}</h2>
          <p className="greeting-sub">
            {formatDate()} &bull; Clinical Overview
          </p>
        </div>
        <div className="system-active-badge">
          <span className="status-pulse-dot" /> SYSTEM ACTIVE
        </div>
      </div>

      {/* ── 3. Quick Action Cards (4-Column Grid) ── */}
      {!faculty && (
        <div className="quick-actions-grid">
          <div
            className="quick-action-card action-primary"
            data-testid="quick-action-new-assessment"
            onClick={() => handleActionClick('/patients/new')}
          >
            <div className="action-icon">{Icons.assessment}</div>
            <div className="action-label">New Assessment</div>
          </div>

          <div
            className="quick-action-card"
            data-testid="quick-action-add-patient"
            onClick={() => handleActionClick('/patients/new')}
          >
            <div className="action-icon">{Icons.addPatient}</div>
            <div className="action-label">Add Patient</div>
          </div>

          <div
            className="quick-action-card"
            data-testid="quick-action-upload-radio"
            onClick={() => handleActionClick('/patients')}
          >
            <div className="action-icon">{Icons.uploadRadio}</div>
            <div className="action-label">Upload Radiology</div>
          </div>

          <div
            className="quick-action-card"
            data-testid="quick-action-generate-report"
            onClick={() => handleActionClick('/patients')}
          >
            <div className="action-icon">{Icons.report}</div>
            <div className="action-label">Generate Report</div>
          </div>
        </div>
      )}

      {/* ── 4. Stat Cards (4-Column Grid) ── */}
      <div className="clinical-stats-grid">
        <div className="stat-box">
          <div className="stat-top">
            <span className="stat-title">PATIENTS</span>
            <span className="stat-tag tag-blue">+12%</span>
          </div>
          <div className="stat-num">{totalCount}</div>
        </div>

        <div className="stat-box">
          <div className="stat-top">
            <span className="stat-title">PENDING</span>
            <span className="stat-tag tag-amber">High</span>
          </div>
          <div className="stat-num">{pendingCount}</div>
        </div>

        <div className="stat-box">
          <div className="stat-top">
            <span className="stat-title">COMPLETED</span>
            <span className="stat-tag tag-green">98%</span>
          </div>
          <div className="stat-num">{completedCount}</div>
        </div>

        <div className="stat-box stat-high-risk">
          <div className="stat-top">
            <span className="stat-title stat-title-danger">HIGH RISK</span>
            <span className="stat-tag tag-danger">Urgent</span>
          </div>
          <div className="stat-num stat-num-danger">{highRiskCount}</div>
        </div>
      </div>

      {/* ── 5. Priority Alerts Section ── */}
      <div className="priority-alerts-section">
        <div className="section-header-row">
          <div className="section-title-with-icon">
            <span className="alert-header-icon">{Icons.alert}</span>
            <span>Priority Alerts</span>
          </div>
          <span className="critical-action-tag">CRITICAL ACTION</span>
        </div>

        {/* Dynamic High-Risk Cases from Live Database */}
        {patients.filter(p => p.assessmentStatus === 'NEEDS_REVISION' || p.asaClassification === 'ASA_III' || p.asaClassification === 'ASA_IV').length === 0 ? (
          <div className="priority-alert-card" style={{ opacity: 0.85, padding: '16px 20px', border: '1px dashed var(--border-color)', background: 'rgba(255,255,255,0.02)' }}>
            <p className="alert-description" style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.9rem', textAlign: 'center' }}>
              No critical priority alerts or high-risk assessments currently flagged.
            </p>
          </div>
        ) : (
          patients
            .filter(p => p.assessmentStatus === 'NEEDS_REVISION' || p.asaClassification === 'ASA_III' || p.asaClassification === 'ASA_IV')
            .slice(0, 3)
            .map(p => (
              <div key={p.id} className="priority-alert-card" data-testid={`priority-alert-${p.id}`} onClick={() => handleActionClick(`/patients/${p.id}`)} style={{ cursor: 'pointer' }}>
                <div className="alert-card-header">
                  <span className="alert-patient-name">{p.fullName} ({p.mrn || `#${p.id}`})</span>
                  <span className="alert-vitals">{p.asaClassification || 'HIGH RISK'}</span>
                </div>
                <p className="alert-description">
                  {p.procedureType ? `Planned Procedure: ${p.procedureType}. ` : ''}
                  Requires immediate clinical review / specialist sign-off before clearance.
                </p>
              </div>
            ))
        )}
      </div>

      {/* ── 6. Clinical Patient Registry (Recent Patients Table) ── */}
      <div className="card clinical-table-card">
        <div className="dashboard-table-header">
          <div className="section-title">
            Recent Patients {searchTerm && <span className="filter-badge">({filteredPatients.length} matches)</span>}
          </div>
          <Link to="/patients" className="btn btn-ghost btn-sm">
            View All Patients &rarr;
          </Link>
        </div>

        {filteredPatients.length === 0 ? (
          <div className="empty-state">
            <h3>No matching patients found</h3>
            <p>
              {searchTerm ? `No patient or case matches "${searchTerm}" in the live database.` : 'No patients registered in the database yet.'}
            </p>
            {!searchTerm && !faculty && (
              <Link to="/patients/new" className="btn btn-primary btn-sm" style={{ marginTop: 14 }}>
                Register First Patient
              </Link>
            )}
          </div>
        ) : (
          <div className="table-wrapper" style={{ marginTop: 14 }}>
            <table>
              <thead>
                <tr>
                  <th>MRN / Case ID</th>
                  <th>Patient Name</th>
                  <th>Procedure</th>
                  <th>Registered By</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredPatients.map(p => (
                  <tr key={p.id}>
                    <td>
                      <code className="mrn-badge">{p.mrn || `#${p.id}`}</code>
                    </td>
                    <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{p.fullName}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{p.procedureType || '—'}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{p.createdByName || '—'}</td>
                    <td><StatusBadge status={p.assessmentStatus} /></td>
                    <td style={{ textAlign: 'right' }}>
                      <Link to={`/patients/${p.id}`} data-testid={`view-case-${p.id}`} className="btn btn-ghost btn-sm">
                        View Case &rarr;
                      </Link>
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

