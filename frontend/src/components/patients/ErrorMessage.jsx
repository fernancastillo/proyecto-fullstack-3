const ErrorMessage = ({ message, onRetry }) => {
  if (!message) return null;

  return (
    <div className="mb-4 flex items-center justify-between gap-3 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
      <div className="flex items-center gap-3">
        <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {message}
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-red-700 hover:text-red-800 font-medium text-sm"
        >
          Reintentar
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;