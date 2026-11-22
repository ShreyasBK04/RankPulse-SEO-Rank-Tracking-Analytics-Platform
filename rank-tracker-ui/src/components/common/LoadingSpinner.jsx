import './LoadingSpinner.css';

export default function LoadingSpinner({ size = 32, text }) {
  return (
    <div className="loading-spinner">
      <svg width={size} height={size} viewBox="0 0 24 24" className="loading-spinner__svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" fill="none" strokeDasharray="31.4 31.4" strokeLinecap="round" />
      </svg>
      {text && <span className="loading-spinner__text">{text}</span>}
    </div>
  );
}
