import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
  CartesianGrid
} from 'recharts';

export default function DistributionBarChart({ distribution, height = 300 }) {
  if (!distribution) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9CA3AF' }}>
        No distribution data
      </div>
    );
  }

  const data = [
    { range: 'Top 3', count: distribution.top3 || 0, color: '#16A34A' },
    { range: 'Top 10', count: distribution.top10 || 0, color: '#4F46E5' },
    { range: 'Top 20', count: distribution.top20 || 0, color: '#6366F1' },
    { range: 'Top 50', count: distribution.top50 || 0, color: '#F59E0B' },
    { range: 'Top 100', count: distribution.top100 || 0, color: '#EC4899' },
    { range: '100+', count: distribution.notRanking || 0, color: '#9CA3AF' }
  ];

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const item = payload[0].payload;
      return (
        <div style={{
          backgroundColor: '#FFFFFF',
          border: '1px solid #E5E7EB',
          borderRadius: '8px',
          padding: '8px 12px',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
        }}>
          <p style={{ margin: 0, fontWeight: 600, color: '#111827', fontSize: '13px' }}>
            {item.range}
          </p>
          <p style={{ margin: '4px 0 0', color: item.color, fontSize: '12px', fontWeight: 500 }}>
            {item.count} {item.count === 1 ? 'keyword' : 'keywords'}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ width: '100%', height }}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 10, right: 10, left: -15, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" vertical={false} />
          <XAxis
            dataKey="range"
            tick={{ fill: '#6B7280', fontSize: 12 }}
            stroke="#E5E7EB"
            tickLine={false}
          />
          <YAxis
            allowDecimals={false}
            tick={{ fill: '#6B7280', fontSize: 12 }}
            stroke="#E5E7EB"
            tickLine={false}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="count" radius={[4, 4, 0, 0]}>
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
