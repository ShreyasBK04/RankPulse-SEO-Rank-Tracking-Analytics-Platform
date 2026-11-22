import './Toast.css';

export default function Toast({ toasts, onRemove }) {
  if (!toasts.length) return null;

  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`}>
          <span className="toast__message">{t.message}</span>
          <button className="toast__close" onClick={() => onRemove(t.id)}>×</button>
        </div>
      ))}
    </div>
  );
}
