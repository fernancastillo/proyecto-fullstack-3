const PatientProfileHeader = ({ patient }) => {
  return (
    <div className="bg-gradient-to-r from-blue-500 to-blue-700 px-6 py-8">
      <div className="flex items-center gap-4">
        <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center">
          <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>
        <div className="text-white">
          <h2 className="text-xl font-semibold">
            {patient.name} {patient.lastname}
          </h2>
          <p className="text-blue-100 text-sm">
            {patient.rut}-{patient.dv}
          </p>
        </div>
      </div>
    </div>
  );
};

export default PatientProfileHeader;