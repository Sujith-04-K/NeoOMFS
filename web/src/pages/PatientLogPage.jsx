import React, { useEffect, useState, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { searchPatients } from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty } from '../utils/auth';

const STATUS_TABS = [
  { id: 'ALL', label: 'All Cases' },
  { id: 'PENDING_REVIEW', label: 'Pending Review', alert: true },
  { id: 'APPROVED', label: 'Approved' },
  { id: 'NEEDS_REVISION', label: 'Needs Revision' },
  { id: 'DRAFT', label: 'Drafts' },
];

export default function PatientLogPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [patients, setPatients] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [status, setStatus] = useState(searchParams.get('status') || 'ALL');
  const [refreshing, setRefreshing] = useState(false);
  const faculty = isFaculty();

  const load = useCallback(async (isManual = false) => {
    if (isManual) setRefreshing(true);
    setLoading(true);
    try {
      const res = await searchPatients({
        search: search || undefined,
        status: status !== 'ALL' ? status : undefined,
        size: 50,
      });
      const list = (res && (Array.isArray(res) ? res : res.content)) || [];
      setPatients(list);
      setTotal(res?.totalElements || list.length);
    } catch (e) {
      console.error('Error loading patient log:', e);
    } finally {
      setLoading(false);
      if (isManual) setRefreshing(false);
    }
  }, [search, status]);

  useEffect(() => {
    load();
  }, [load]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setSearchParams({ search, status });
    load();
  };

  const handleTabChange = (newStatus) => {
    setStatus(newStatus);
    setSearchParams({ search, status: newStatus });
  };

  return (
    <div className="dashboard-container">
      {/* ── 1. Page Header & Actions ── */}
      <div className="dashboard-header-row" style={{ alignItems: 'center' }}>
        <div className="greeting-block">
          <h2>Clinical Patient Registry</h2>
          <p className="greeting-sub">
            {total} total records in shared database &bull; Search MRN, patient name, or clinical plan
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button
            className={`refresh-btn ${refreshing ? 'spinning' : ''}`}
            data-testid="log-sync-btn"
            onClick={() => load(true)}
            title="Sync with Backend Database"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="23 4 23 10 17 10"></polyline>
              <polyline points="1 20 1 14 7 14"></polyline>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
            </svg>
            <span>Sync DB</span>
          </button>
          {!faculty && (
            <Link to="/patients/new" data-testid="log-register-patient-btn" className="btn btn-primary" style={{ padding: '12px 20px' }}>
              + Register New Patient
            </Link>
          )}
        </div>
      </div>

      {/* ── 2. Search & Status Filter Tabs ── */}
      <div className="card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <form onSubmit={handleSearchSubmit} className="dashboard-search-row" style={{ margin: 0 }}>
          <div className="dashboard-search-bar">
            <span className="search-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </span>
            <input
              type="text"
              data-testid="log-search-input"
              placeholder="Search patient name, MRN, case ID, or procedure..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            {search && (
              <button
                type="button"
                className="clear-search-btn"
                data-testid="log-clear-search"
                onClick={() => { setSearch(''); setSearchParams({ search: '', status }); }}
              >
                ✕
              </button>
            )}
          </div>
          <button type="submit" data-testid="log-filter-btn" className="btn btn-primary" style={{ padding: '12px 24px' }}>
            Filter Records
          </button>
        </form>

        {/* Clinical Status Filter Pills */}
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', borderTop: '1px solid var(--border)', paddingTop: '16px' }}>
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              data-testid={`log-status-tab-${tab.id}`}
              onClick={() => handleTabChange(tab.id)}
              style={{
                background: status === tab.id ? 'var(--accent)' : 'var(--surface)',
                color: status === tab.id ? '#fff' : 'var(--text-secondary)',
                border: `1px solid ${status === tab.id ? 'var(--accent)' : 'var(--border)'}`,
                padding: '8px 16px',
                borderRadius: '20px',
                fontSize: '0.82rem',
                fontWeight: 700,
                cursor: 'pointer',
                transition: 'var(--transition)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              {tab.label}
              {tab.alert && status !== tab.id && (
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--amber)' }} />
              )}
            </button>
          ))}
        </div>
      </div>

      {/* ── 3. Patient Registry Table ── */}
      <div className="card clinical-table-card">
        {loading ? (
          <div className="loading" style={{ padding: '40px 0' }}>
            <span className="spinner" /> Loading shared clinical database…
          </div>
        ) : patients.length === 0 ? (
          <div className="empty-state" style={{ padding: '48px 0' }}>
            <h3>No patient records found</h3>
            <p>
              {search || status !== 'ALL'
                ? 'Try clearing your search query or selecting a different status tab.'
                : 'No patients have been registered in the database yet.'}
            </p>
            {!faculty && !search && status === 'ALL' && (
              <Link to="/patients/new" data-testid="log-empty-register-btn" className="btn btn-primary" style={{ marginTop: '16px' }}>
                + Register First Patient
              </Link>
            )}
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>MRN / Case ID</th>
                  <th>Patient Name</th>
                  <th>Age / Gender</th>
                  <th>Procedure Plan</th>
                  <th>ASA Risk Level</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th style={{ textAlign: 'right' }}>Clinical Action</th>
                </tr>
              </thead>
              <tbody>
                {patients.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <code className="mrn-badge">{p.mrn || `#${p.id}`}</code>
                    </td>
                    <td style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{p.fullName}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>
                      {p.age ? `${p.age}y` : '—'} &bull; {p.gender || '—'}
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontWeight: 500 }}>
                      {p.procedureType || '—'}
                    </td>
                    <td>
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '3px 8px',
                          borderRadius: '6px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          background:
                            p.asaClassification === 'ASA_III' || p.asaClassification === 'ASA_IV'
                              ? 'var(--red-bg)'
                              : 'var(--blue-bg)',
                          color:
                            p.asaClassification === 'ASA_III' || p.asaClassification === 'ASA_IV'
                              ? 'var(--red)'
                              : '#818cf8',
                        }}
                      >
                        {p.asaClassification ? p.asaClassification.replace('_', ' ') : 'ASA I'}
                      </span>
                    </td>
                    <td>
                      <StatusBadge status={p.assessmentStatus} />
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      {p.createdAt ? p.createdAt.split(' ')[0] : 'Today'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <Link to={`/patients/${p.id}`} data-testid={`log-view-case-${p.id}`} className="btn btn-ghost btn-sm">
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

