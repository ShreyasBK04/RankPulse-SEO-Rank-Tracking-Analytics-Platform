import React, { useState } from 'react';
import Modal from '../common/Modal';

export default function AddKeywordModal({ open, onClose, onSubmit, isSubmitting }) {
  const [keyword, setKeyword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const clean = keyword.trim();
    if (!clean) {
      setError('Keyword cannot be empty');
      return;
    }
    if (clean.length < 2) {
      setError('Keyword must be at least 2 characters');
      return;
    }
    setError('');
    onSubmit(clean);
  };

  const handleClose = () => {
    setKeyword('');
    setError('');
    onClose();
  };

  return (
    <Modal open={open} onClose={handleClose} title="Add New Keyword" maxWidth={440}>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#374151', marginBottom: 6 }}>
            Search Keyword / Query
          </label>
          <input
            type="text"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value);
              if (error) setError('');
            }}
            placeholder="e.g. spring boot tutorial"
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
          <p style={{ margin: '6px 0 0', fontSize: 12, color: '#6B7280' }}>
            This keyword will be checked against Google search results for your domain.
          </p>
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
            disabled={isSubmitting || !keyword.trim()}
          >
            {isSubmitting ? 'Adding...' : 'Add Keyword'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
