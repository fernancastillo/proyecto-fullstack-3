const PatientInfoAlert = () => {
  return (
    <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
      <div className="flex items-start gap-3">
        <svg className="w-5 h-5 text-blue-600 mt-0.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div className="text-sm text-blue-800">
          <p className="font-medium mb-1">Información importante:</p>
          <p className="text-blue-700">El RUT no puede ser modificado por seguridad. Si necesitas corregirlo, contacta al soporte.</p>
        </div>
      </div>
    </div>
  );
};

export default PatientInfoAlert;