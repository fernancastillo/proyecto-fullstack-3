import { useState } from 'react';
import DashboardTab from '../components/admin/DashboardTab';
import MedicosAdminsTab from '../components/admin/MedicosAdminsTab';
import PacientesTab from '../components/admin/PacientesTab';
import CrearUsuarioTab from '../components/admin/CrearUsuarioTab';

const TABS = [
  { id: 'dashboard',  label: 'Dashboard'           },
  { id: 'medicos',    label: 'Médicos y Admins'    },
  { id: 'pacientes',  label: 'Pacientes'          },
  { id: 'crear',      label: 'Crear Usuario'         },
];

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <div>
      <h1 className="text-2xl font-bold text-blue-700 mb-6">Panel de Administración</h1>

      {/* Navegación por tabs */}
      <div className="flex gap-2 border-b border-gray-200 mb-6 overflow-x-auto">
        {TABS.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`px-4 py-2 text-sm font-medium rounded-t-lg whitespace-nowrap transition-colors ${
              activeTab === tab.id
                ? 'bg-blue-700 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Contenido del tab activo */}
      {activeTab === 'dashboard' && <DashboardTab />}
      {activeTab === 'medicos'   && <MedicosAdminsTab />}
      {activeTab === 'pacientes' && <PacientesTab />}
      {activeTab === 'crear'     && <CrearUsuarioTab />}
    </div>
  );
};

export default AdminPage;