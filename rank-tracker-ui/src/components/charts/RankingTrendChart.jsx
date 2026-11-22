import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Legend
} from 'recharts';

export default function RankingTrendChart({ data, height = 300 }) {
  if (!data || data.length === 0) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9CA3AF' }}>
        No trend data available
      </div>
    );
  }

  // Formatting date for X-axis
  const formatDate = (str) => {
    if (!str) return '';
    const parts = str.split('-');
    if (parts.length === 3) return `${parts[1]}/${parts[2]}`;
    return str;
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div style={{
          backgroundColor: '#FFFFFF',
          border: '1px solid #E5E7EB',
          borderRadius: '8px',
          padding: '10px 14px',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
        }}>
          <p style={{ fontWeight: 600, margin: '0 0 6px', color: '#111827', fontSize: '13px' }}>
            Date: {label}
          </p>
          <p style={{ margin: 0, color: '#4F46E5', fontSize: '12.5px', fontWeight: 500 }}>
            Avg Position: #{payload[0].value}
          </p>
          {payload[0].payload.keywordsTracked !== undefined && (
            <p style={{ margin: '4px 0 0', color: '#6B7280', fontSize: '12px' }}>
              Keywords Tracked: {payload[0].payload.keywordsTracked}
            </p>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ width: '100%', height }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" vertical={false} />
          <XAxis
            dataKey="date"
            tickFormatter={formatDate}
            tick={{ fill: '#6B7280', fontSize: 12 }}
            stroke="#E5E7EB"
            tickLine={false}
          />
          <YAxis
            reversed={true} // #1 is best, so rank 1 at top!
            domain={[1, 100]}
            tick={{ fill: '#6B7280', fontSize: 12 }}
            stroke="#E5E7EB"
            tickLine={false}
            tickFormatter={(val) => `#${val}`}
          />
          <Tooltip content={<CustomTooltip />} />
          <Line
            type="monotone"
            dataKey="averagePosition"
            name="Average Position"
            stroke="#4F46E5"
            strokeWidth={2.5}
            dot={{ r: 3, fill: '#4F46E5', strokeWidth: 0 }}
            activeDot={{ r: 6, fill: '#4F46E5', stroke: '#EEF2FF', strokeWidth: 3 }}
            connectNulls={true}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
