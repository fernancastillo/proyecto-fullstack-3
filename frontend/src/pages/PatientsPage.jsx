import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import PatientProfileHeader from '../components/patients/PatientProfileHeader';
import PatientProfileForm from '../components/patients/PatientProfileForm';
import PatientInfoAlert from '../components/patients/PatientInfoAlert';
import LoadingSpinner from '../components/patients/LoadingSpinner';
import SuccessMessage from '../components/patients/SuccessMessage';

// Función para crear perfil básico por defecto
const createDefaultPatient = (user) => {
  const nameParts = user?.username?.split(' ') || ['Usuario'];
  
  return {
    id: Date.now(),
    rut: 'PENDIENTE',
    dv: '0',
    name: nameParts[0] || 'Usuario',
    lastname: nameParts[1] || 'Nuevo',
    email: user?.email || '',
    phone: '+56900000000',
    region: '',
    comuna: '',
    address: ''
  };
};

const PatientsPage = () => {
  const { user } = useAuth();
  const [patient, setPatient] = useState(null);
  const [allPatients, setAllPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [isFirstTime, setIsFirstTime] = useState(false);

  // Cargar datos del paciente y todos los pacientes
  useEffect(() => {
    loadAllData();
  }, [user]);

  const loadAllData = () => {
    setLoading(true);
    setTimeout(() => {
      // Cargar o inicializar la lista global de pacientes (ÚNICA FUENTE DE VERDAD)
      let storedAllPatients = localStorage.getItem('patients');
      let patientsList = [];
      
      if (storedAllPatients) {
        patientsList = JSON.parse(storedAllPatients);
      } else {
        // Inicializar lista vacía
        patientsList = [];
        localStorage.setItem('patients', JSON.stringify(patientsList));
      }
      setAllPatients(patientsList);
      
      // Buscar el paciente actual por email en la lista global
      const currentPatient = patientsList.find(p => p.email === user?.email);
      
      if (currentPatient) {
        // Usuario existente con datos guardados
        setPatient(currentPatient);
        setIsFirstTime(false);
      } else if (user) {
        // Primera vez del usuario - crear perfil por defecto
        const defaultPatient = createDefaultPatient(user);
        
        // Agregar a la lista global
        const updatedPatients = [...patientsList, defaultPatient];
        localStorage.setItem('patients', JSON.stringify(updatedPatients));
        setAllPatients(updatedPatients);
        setPatient(defaultPatient);
        setIsFirstTime(true);
        setSuccessMessage('¡Bienvenido! Por favor completa tus datos personales.');
      } else {
        // No hay usuario logueado (no debería ocurrir por el ProtectedRoute)
        setPatient(null);
      }
      setLoading(false);
    }, 500);
  };

  const handleSave = (formData) => {
    setIsSaving(true);
    
    setTimeout(() => {
      const updatedPatient = { ...patient, ...formData };
      
      // Actualizar el paciente en la lista global de pacientes
      const updatedAllPatients = allPatients.map(p => 
        p.id === patient.id ? updatedPatient : p
      );
      
      // Guardar SOLO en la lista global (única fuente de verdad)
      localStorage.setItem('patients', JSON.stringify(updatedAllPatients));
      setAllPatients(updatedAllPatients);
      setPatient(updatedPatient);
      
      setIsSaving(false);
      setIsEditing(false);
      setSuccessMessage('¡Tus datos han sido actualizados correctamente!');
      
      // Limpiar mensaje después de 3 segundos
      setTimeout(() => setSuccessMessage(''), 3000);
    }, 800);
  };

  const handleCancel = () => {
    setIsEditing(false);
  };

  const clearSuccessMessage = () => {
    setSuccessMessage('');
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!patient) {
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center">
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8 text-center">
          <svg className="w-16 h-16 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h2 className="text-xl font-semibold text-slate-800 mb-2">Error al cargar perfil</h2>
          <p className="text-slate-600">No se pudo cargar la información del paciente.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Header */}
        <div className="mb-8">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-2xl font-semibold text-slate-800 tracking-tight">
                Mi Perfil
              </h1>
              <p className="text-sm text-slate-500 mt-1">
                {isFirstTime 
                  ? 'Completa tus datos personales para continuar' 
                  : 'Consulta y actualiza tus datos personales'}
              </p>
            </div>
            {!isEditing && !isFirstTime && (
              <button
                onClick={() => setIsEditing(true)}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors duration-150 flex items-center gap-2"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                </svg>
                Editar Perfil
              </button>
            )}
          </div>
        </div>

        {/* Success Message */}
        <SuccessMessage message={successMessage} onClose={clearSuccessMessage} />

        {/* Patient Card */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <PatientProfileHeader patient={patient} />
          <PatientProfileForm
            patient={patient}
            isEditing={isEditing || isFirstTime}
            onSave={handleSave}
            onCancel={handleCancel}
            isSaving={isSaving}
            allPatients={allPatients}
          />
        </div>

        {/* Informative Alert */}
        <div className="mt-6">
          <PatientInfoAlert />
        </div>

        {/* First time banner */}
        {isFirstTime && !isEditing && (
          <div className="mt-4 bg-yellow-50 border border-yellow-200 rounded-xl p-4">
            <div className="flex items-start gap-3">
              <svg className="w-5 h-5 text-yellow-600 mt-0.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div className="text-sm text-yellow-800">
                <p className="font-medium mb-1">Perfil incompleto</p>
                <p className="text-yellow-700">Completa tus datos personales (RUT, teléfono, dirección, etc.) para finalizar tu registro.</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PatientsPage;