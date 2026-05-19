import { useState, useEffect } from 'react';
import api from '../../api/axios';

// ─── Donut Chart ──────────────────────────────────────────────
const DonutChart = ({ data }) => {
  const total = data.reduce((acc, d) => acc + d.value, 0);
  if (total === 0) return <p className="text-gray-400 text-sm py-4">Sin datos disponibles.</p>;

  const r = 50;
  const size = 140;
  const cx = size / 2;
  const cy = size / 2;
  const strokeWidth = 20;
  const circumference = 2 * Math.PI * r;

  let offset = 0;
  const slices = data.map(d => {
    const dash  = (d.value / total) * circumference;
    const slice = { ...d, dash, gap: circumference - dash, offset };
    offset += dash;
    return slice;
  });

  return (
    <div className="flex items-center gap-6">
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ transform: 'rotate(-90deg)' }}>
        {slices.map((s, i) => (
          <circle
            key={i}
            cx={cx} cy={cy} r={r}
            fill="none"
            stroke={s.color}
            strokeWidth={strokeWidth}
            strokeDasharray={`${s.dash} ${s.gap}`}
            strokeDashoffset={-s.offset}
          />
        ))}
      </svg>
      <div className="space-y-2 flex-1">
        {data.map((d, i) => (
          <div key={i} className="flex items-center justify-between gap-3 text-sm">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ backgroundColor: d.color }} />
              <span className="text-slate-600">{d.label}</span>
            </div>
            <span className="font-semibold text-slate-800 tabular-nums">{d.value}</span>
          </div>
        ))}
        <p className="text-xs text-slate-400 pt-1">Total: {total}</p>
      </div>
    </div>
  );
};

// ─── Bar Chart ────────────────────────────────────────────────
const BarChart = ({ data }) => {
  const max = Math.max(...data.map(d => d.value), 1);
  return (
    <div className="flex items-end gap-4" style={{ height: '120px' }}>
      {data.map((d, i) => (
        <div key={i} className="flex flex-col items-center gap-1 flex-1 h-full justify-end">
          <span className="text-xs font-semibold text-slate-700 tabular-nums">{d.value}</span>
          <div
            className="w-full rounded-t-md transition-all duration-500"
            style={{
              backgroundColor: d.color,
              height: `${Math.max((d.value / max) * 80, d.value > 0 ? 4 : 0)}px`,
            }}
          />
          <span className="text-xs text-slate-500 text-center leading-tight mt-1">{d.label}</span>
        </div>
      ))}
    </div>
  );
};

// ─── Stat Card ────────────────────────────────────────────────
const StatCard = ({ label, value, colorClass, icon }) => (
  <div className={`rounded-xl p-5 ${colorClass} flex items-start justify-between gap-2`}>
    <div>
      <p className="text-4xl font-bold tabular-nums">{value ?? '—'}</p>
      <p className="text-sm mt-1 font-medium opacity-80">{label}</p>
    </div>
    <span className="text-3xl opacity-50 flex-shrink-0">{icon}</span>
  </div>
);

// ─── Dashboard Tab ────────────────────────────────────────────
const DashboardTab = () => {
  const [data,    setData]    = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');

  useEffect(() => {
    api.get('/dashboard/admin')
      .then(res => setData(res.data))
      .catch(() => setError('No se pudieron cargar los datos del dashboard.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex items-center justify-center py-20">
      <div className="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full" />
    </div>
  );
  if (error) return <p className="text-red-500 bg-red-50 p-3 rounded-lg">{error}</p>;

  const solicitudes = data.solicitudes ?? [];
  const usuarios    = data.usuarios    ?? [];

  const porEstado = {
    PENDIENTE:  solicitudes.filter(s => s.estado === 'PENDIENTE').length,
    CONFIRMADA: solicitudes.filter(s => s.estado === 'CONFIRMADA').length,
    CANCELADA:  solicitudes.filter(s => s.estado === 'CANCELADA').length,
  };

  const porRol = {
    PACIENTE: usuarios.filter(u => u.role === 'PACIENTE').length,
    MEDICO:   usuarios.filter(u => u.role === 'MEDICO').length,
    ADMIN:    usuarios.filter(u => u.role === 'ADMIN').length,
  };

  const stats = [
    { label: 'Usuarios registrados',  value: data.totalUsuarios,       colorClass: 'bg-blue-100   text-blue-700',   icon: '' },
    { label: 'Médicos',               value: data.totalMedicos,         colorClass: 'bg-green-100  text-green-700',  icon: '' },
    { label: 'Solicitudes totales',   value: data.totalSolicitudes,     colorClass: 'bg-yellow-100 text-yellow-700', icon: '' },
    { label: 'Solicitudes activas',   value: data.solicitudesActivas ?? porEstado.PENDIENTE, colorClass: 'bg-orange-100 text-orange-700', icon: '' },
    { label: 'En lista de espera',    value: data.totalEnEspera,        colorClass: 'bg-purple-100 text-purple-700', icon: '' },
    { label: 'Especialidades',        value: data.totalEspecialidades,  colorClass: 'bg-teal-100   text-teal-700',   icon: '' },
  ];

  const donutData = [
    { label: 'Pendiente',  value: porEstado.PENDIENTE,  color: '#f59e0b' },
    { label: 'Confirmada', value: porEstado.CONFIRMADA, color: '#22c55e' },
    { label: 'Cancelada',  value: porEstado.CANCELADA,  color: '#ef4444' },
  ];

  const barData = [
    { label: 'Pacientes', value: porRol.PACIENTE, color: '#60a5fa' },
    { label: 'Médicos',   value: porRol.MEDICO,   color: '#34d399' },
    { label: 'Admins',    value: porRol.ADMIN,    color: '#a78bfa' },
  ];

  const ultimasSolicitudes = [...solicitudes]
    .sort((a, b) => b.id - a.id)
    .slice(0, 5);

  return (
    <div className="space-y-8">

      {/* Tarjetas de métricas */}
      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {stats.map(s => <StatCard key={s.label} {...s} />)}
      </div>

      {/* Gráficos */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Solicitudes por estado</h2>
          <DonutChart data={donutData} />
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Usuarios por rol</h2>
          <BarChart data={barData} />
        </div>
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
                        s.estado === 'CONFIRMADA' ? 'bg-green-100  text-green-700'  :
                        s.estado === 'CANCELADA'  ? 'bg-red-100    text-red-700'    :
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