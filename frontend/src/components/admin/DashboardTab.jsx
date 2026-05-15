import { useState, useEffect } from 'react';
import api from '../../api/axios';

const StatCard = ({ label, value, colorClass }) => (
  <div className={`rounded-xl p-5 ${colorClass}`}>
    <p className="text-4xl font-bold">{value}</p>
    <p className="text-sm mt-1 font-medium opacity-80">{label}</p>
  </div>
);

const DashboardTab = () => {
  const [data, setData]       = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    api.get('/dashboard/admin')
      .then(res => setData(res.data))
      .catch(() => setError('No se pudieron cargar los datos del dashboard.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-gray-500 animate-pulse">Cargando datos...</p>;
  if (error)   return <p className="text-red-500 bg-red-50 p-3 rounded-lg">{error}</p>;

  const stats = [
    { label: 'Total Usuarios',       value: data.totalUsuarios,    colorClass: 'bg-blue-100 text-blue-700'   },
    { label: 'Médicos Registrados',  value: data.totalMedicos,     colorClass: 'bg-green-100 text-green-700' },
    { label: 'Solicitudes',          value: data.totalSolicitudes, colorClass: 'bg-yellow-100 text-yellow-700' },
    { label: 'En Lista de Espera',   value: data.totalEnEspera,    colorClass: 'bg-purple-100 text-purple-700' },
  ];

  // Últimas 5 solicitudes (las más recientes por ID)
  const ultimasSolicitudes = [...(data.solicitudes || [])]
    .sort((a, b) => b.id - a.id)
    .slice(0, 5);

  return (
    <div className="space-y-8">
      {/* Tarjetas de estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {stats.map(s => (
          <StatCard key={s.label} {...s} />
        ))}
      </div>

      {/* Últimas solicitudes */}
      <div>
        <h2 className="text-base font-semibold text-gray-700 mb-3">Últimas 5 solicitudes</h2>
        {ultimasSolicitudes.length === 0 ? (
          <p className="text-gray-500 text-sm">No hay solicitudes registradas.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border border-gray-200 rounded-lg overflow-hidden">
              <thead className="bg-gray-100 text-gray-700">
                <tr>
                  <th className="px-4 py-2 text-left">ID</th>
                  <th className="px-4 py-2 text-left">Especialidad</th>
                  <th className="px-4 py-2 text-left">Estado</th>
                  <th className="px-4 py-2 text-left">Fecha solicitud</th>
                </tr>
              </thead>
              <tbody>
                {ultimasSolicitudes.map(s => (
                  <tr key={s.id} className="border-t border-gray-100">
                    <td className="px-4 py-2 text-gray-500">#{s.id}</td>
                    <td className="px-4 py-2">{s.especialidad}</td>
                    <td className="px-4 py-2">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                        s.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-700'  :
                        s.estado === 'CANCELADA'  ? 'bg-red-100   text-red-700'   :
                                                    'bg-yellow-100 text-yellow-700'
                      }`}>
                        {s.estado}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-gray-500">
                      {s.fechaSolicitud
                        ? new Date(s.fechaSolicitud).toLocaleDateString('es-CL')
                        : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardTab;