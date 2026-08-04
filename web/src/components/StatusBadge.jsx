import React from 'react';

const MAP = {
  DRAFT:         { label: 'Draft',          cls: 'badge-draft'    },
  PENDING_REVIEW:{ label: 'Pending Review', cls: 'badge-pending'  },
  APPROVED:      { label: 'Approved',       cls: 'badge-approved' },
  NEEDS_REVISION:{ label: 'Needs Revision', cls: 'badge-revision' },
};

export default function StatusBadge({ status }) {
  const s = (status || '').toUpperCase();
  const { label, cls } = MAP[s] || { label: s || 'Unknown', cls: 'badge-draft' };
  return <span className={`badge ${cls}`}>{label}</span>;
}
