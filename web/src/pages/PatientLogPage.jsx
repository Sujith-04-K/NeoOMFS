import React, { useEffect, useState, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { searchPatients } from '../api/patients';
import StatusBadge from '../components/StatusBadge';
import { isFaculty } from '../utils/auth';

const STATUSES = ['ALL', 'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'NEEDS_REVISION'];

export default function PatientLogPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [patients, setPatients] = useState([]);
  const [total,    setTotal]    = useState(0);
  const [loading,  setLoading]  = useState(true);
  const [search,   setSearch]   = useState(searchParams.get('search') || '');
  const [status,   setStatus]   = useState(searchParams.get('status') || 'ALL');
  const faculty = isFaculty();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await searchPatients({ search: search || undefined, status: status !== 'ALL' ? status : undefined, size: 50 });
      setPatients((res && (Array.isArray(res) ? res : res.content)) || []);
      setTotal(res?.totalElements || (Array.isArray(res) ? res.length : 0));
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [search, status]);

  useEffect(() => { load(); }, [load]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearchParams({ search, status });
    load();
  };

  return (
    <div>
      <div className="page-header">
        <h1>Patient Log</h1>
        <p>{total} total records · Filter by name, MRN, or status</p>
      </div>

      {/* Filter Bar */}
      <form onSubmit={handleSearch} className="filter-bar">
        <input
          className="form-input"
          placeholder="Search name or MRN…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ maxWidth: 280 }}
        />
        <select
          className="form-input form-select"
          value={status}
          onChange={e => setStatus(e.target.value)}
          style={{ maxWidth: 200 }}
        >
          {STATUSES.map(s => (
            <option key={s} value={s}>
              {s === 'ALL' ? 'All Statuses' : s.replace('_', ' ')}
            </option>
          ))}
        </select>
        <button type="submit" className="btn btn-primary">Apply Filters</button>
        {!faculty && (
          <Link to="/patients/new" className="btn btn-ghost" style={{ marginLeft: 'auto' }}>
            + New Patient
          </Link>
        )}
      </form>

      {/* Table */}
      {loading ? (
        <div className="loading"><span className="spinner" /> Loading patients…</div>
      ) : patients.length === 0 ? (
        <div className="empty-state">
          <h3>No patients found</h3>
          <p>Try adjusting your search filters</p>
        </div>
      ) : (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>MRN</th>
                <th>Patient Name</th>
                <th>Age / Gender</th>
                <th>Procedure</th>
                <th>Registered By</th>
                <th>Status</th>
                <th>Date</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {patients.map(p => (
                <tr key={p.id}>
                  <td>
                    <code style={{ color: 'var(--accent)', fontSize: '.8rem' }}>
                      {p.mrn || `#${p.id}`}
                    </code>
                  </td>
                  <td style={{ fontWeight: 600 }}>{p.fullName}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>
                    {p.age ? `${p.age}y` : '—'} · {p.gender || '—'}
                  </td>
                  <td style={{ color: 'var(--text-secondary)' }}>{p.procedureType || '—'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>{p.createdByName || '—'}</td>
                  <td><StatusBadge status={p.assessmentStatus} /></td>
                  <td style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>
                    {p.createdAt ? p.createdAt.split(' ')[0] : '—'}
                  </td>
                  <td>
                    <Link to={`/patients/${p.id}`} className="btn btn-ghost btn-sm">
                      View →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
