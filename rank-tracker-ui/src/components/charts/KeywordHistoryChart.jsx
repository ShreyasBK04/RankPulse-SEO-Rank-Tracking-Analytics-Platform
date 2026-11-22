import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts';

export default function KeywordHistoryChart({ history, height = 260 }) {
  if (!history || history.length === 0) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9CA3AF' }}>
        No ranking history recorded yet
      </div>
    );
  }

  const formatDate = (str) => {
    if (!str) return '';
    const parts = str.split('-');
    if (parts.length === 3) return `${parts[1]}/${parts[2]}`;
    return str;
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const pos = payload[0].value;
      return (
        <div style={{
          backgroundColor: '#FFFFFF',
          border: '1px solid #E5E7EB',
          borderRadius: '8px',
          padding: '8px 12px',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
        }}>
          <p style={{ margin: 0, color: '#6B7280', fontSize: '12px' }}>Date: {label}</p>
          <p style={{ margin: '4px 0 0', fontWeight: 600, color: '#4F46E5', fontSize: '13px' }}>
            Position: {pos != null ? `#${pos}` : '100+ (Unranked)'}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ width: '100%', height }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={history} margin={{ top: 10, right: 15, left: -10, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" vertical={false} />
          <XAxis
            dataKey="date"
            tickFormatter={formatDate}
            tick={{ fill: '#6B7280', fontSize: 11 }}
            stroke="#E5E7EB"
            tickLine={false}
          />
          <YAxis
            reversed={true} // #1 rank at top
            domain={[1, 100]}
            tick={{ fill: '#6B7280', fontSize: 11 }}
            stroke="#E5E7EB"
            tickLine={false}
            tickFormatter={(val) => `#${val}`}
          />
          <Tooltip content={<CustomTooltip />} />
          <Line
            type="monotone"
            dataKey="position"
            stroke="#4F46E5"
            strokeWidth={2.5}
            dot={{ r: 3, fill: '#4F46E5' }}
            activeDot={{ r: 6 }}
            connectNulls={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
