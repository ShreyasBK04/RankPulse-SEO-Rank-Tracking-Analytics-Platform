import React, { useState } from 'react';
import Modal from '../common/Modal';

export default function AddWebsiteModal({ open, onClose, onSubmit, isSubmitting }) {
  const [domain, setDomain] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  const domainRegex = /^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$/;

  const handleSubmit = (e) => {
    e.preventDefault();
    const cleanDomain = domain.trim().toLowerCase().replace(/^https?:\/\//, '').replace(/\/.*$/, '');
    if (!cleanDomain) {
      setError('Domain is required');
      return;
    }
    if (!domainRegex.test(cleanDomain)) {
      setError('Please enter a valid domain (e.g. example.com)');
      return;
    }
    setError('');
    onSubmit({
      domain: cleanDomain,
      name: name.trim() || cleanDomain
    });
  };

  const handleClose = () => {
    setDomain('');
    setName('');
    setError('');
    onClose();
  };

  return (
    <Modal open={open} onClose={handleClose} title="Add New Website" maxWidth={460}>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 14 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#374151', marginBottom: 6 }}>
            Domain Name *
          </label>
          <input
            type="text"
            value={domain}
            onChange={(e) => {
              setDomain(e.target.value);
              if (error) setError('');
            }}
            placeholder="e.g. embarkx.com"
            maxLength={255}
            autoFocus
            style={{
              width: '100%',
              padding: '9px 12px',
              border: `1px solid ${error ? '#DC2626' : '#D1D5DB'}`,
              borderRadius: '6px',
              fontSize: 13.5,
              outline: 'none',
              backgroundColor: '#FFFFFF',
              color: '#111827'
            }}
          />
          {error && (
            <p style={{ margin: '6px 0 0', fontSize: 12, color: '#DC2626' }}>{error}</p>
          )}
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#374151', marginBottom: 6 }}>
            Website Name (Optional)
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. EmbarkX Blog"
            maxLength={255}
            style={{
              width: '100%',
              padding: '9px 12px',
              border: '1px solid #D1D5DB',
              borderRadius: '6px',
              fontSize: 13.5,
              outline: 'none',
              backgroundColor: '#FFFFFF',
              color: '#111827'
            }}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 24 }}>
          <button
            type="button"
            className="btn btn--secondary"
            onClick={handleClose}
            disabled={isSubmitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn--primary"
            disabled={isSubmitting || !domain.trim()}
          >
            {isSubmitting ? 'Adding...' : 'Add Website'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
