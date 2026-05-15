import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axios';
import ConsultasModal from './ConsultasModal';

const ESPECIALIDADES = [
  'Cardiología', 'Pediatría', 'Neurología',
  'Traumatología', 'Medicina General', 'Dermatología', 'Ginecología',
];

const ROLES_DISPONIBLES = ['MEDICO', 'ADMIN'];

const rolBadge = (role) => {
  const clases = {
    ADMIN:   'bg-purple-100 text-purple-700',
    MEDICO:  'bg-blue-100   text-blue-700',
    PACIENTE:'bg-gray-100   text-gray-600',
  };
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${clases[role] ?? 'bg-gray-100 text-gray-600'}`}>
      {role}
    </span>
  );
};

// ─── Panel de métricas ────────────────────────────────────────
const MetricasPanel = ({ medico }) => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const res = await api.get(`/requests/medico/${medico.id}`);
        setRequests(res.data);
      } catch {
        setError('No se pudieron cargar las métricas.');
      } finally {
        setLoading(false);
      }
    };
    fetchMetrics();
  }, [medico.id]);

  if (loading) return (
    <tr>
      <td colSpan={6} className="px-6 py-3 bg-blue-50 text-sm text-gray-400 animate-pulse">
        Cargando métricas...
      </td>
    </tr>
  );

  if (error) return (
    <tr>
      <td colSpan={6} className="px-6 py-3 bg-red-50 text-sm text-red-500">
        {error}
      </td>
    </tr>
  );

  const total       = requests.length;
  const pendientes  = requests.filter(r => r.estado === 'PENDIENTE').length;
  const confirmadas = requests.filter(r => r.estado === 'CONFIRMADA').length;
  const canceladas  = requests.filter(r => r.estado === 'CANCELADA').length;

  const ahora = new Date();
  const proximaCita = requests
    .filter(r => r.estado === 'CONFIRMADA' && r.fechaCita && new Date(r.fechaCita) > ahora)
    .sort((a, b) => new Date(a.fechaCita) - new Date(b.fechaCita))[0];

  const ultimaSolicitud = requests
    .sort((a, b) => new Date(b.fechaSolicitud) - new Date(a.fechaSolicitud))[0];

  const fmtFecha = (iso) => {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('es-CL', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  return (
    <tr>
      <td colSpan={6} className="bg-slate-50 border-t border-blue-100 px-6 py-4">
        <div className="flex items-center gap-2 mb-3">
          <span className="text-xs font-semibold text-blue-700 uppercase tracking-wide">
            Métricas — {medico.name} {medico.lastname}
          </span>
          {medico.especialidad && (
            <span className="text-xs text-gray-400">· {medico.especialidad}</span>
          )}
        </div>

        {/* Tarjetas de conteo */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-4">
          <div className="bg-blue-50 border border-blue-200 rounded-lg px-4 py-3 text-center">
            <p className="text-2xl font-bold text-blue-700">{total}</p>
            <p className="text-xs text-blue-500 mt-0.5">Total solicitudes</p>
          </div>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg px-4 py-3 text-center">
            <p className="text-2xl font-bold text-yellow-600">{pendientes}</p>
            <p className="text-xs text-yellow-500 mt-0.5">Pendientes</p>
          </div>
          <div className="bg-green-50 border border-green-200 rounded-lg px-4 py-3 text-center">
            <p className="text-2xl font-bold text-green-700">{confirmadas}</p>
            <p className="text-xs text-green-500 mt-0.5">Confirmadas</p>
          </div>
          <div className="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-center">
            <p className="text-2xl font-bold text-red-600">{canceladas}</p>
            <p className="text-xs text-red-400 mt-0.5">Canceladas</p>
          </div>
        </div>

        {/* Próxima cita y última solicitud */}
        <div className="flex flex-wrap gap-6 text-sm">
          <div>
            <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Próxima cita confirmada</span>
            <p className={`mt-0.5 font-medium ${proximaCita ? 'text-green-700' : 'text-gray-400 italic'}`}>
              {proximaCita ? fmtFecha(proximaCita.fechaCita) : 'Sin citas próximas'}
            </p>
          </div>
          <div>
            <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Última solicitud recibida</span>
            <p className={`mt-0.5 font-medium ${ultimaSolicitud ? 'text-gray-700' : 'text-gray-400 italic'}`}>
              {ultimaSolicitud ? fmtFecha(ultimaSolicitud.fechaSolicitud) : 'Sin solicitudes'}
            </p>
          </div>
          {total > 0 && (
            <div>
              <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Tasa de confirmación</span>
              <p className="mt-0.5 font-medium text-gray-700">
                {total > 0 ? Math.round((confirmadas / total) * 100) : 0}%
              </p>
            </div>
          )}
        </div>
      </td>
    </tr>
  );
};

// ─── Componente principal ─────────────────────────────────────
const MedicosAdminsTab = () => {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  const [consultasMedico, setConsultasMedico] = useState(null);
  const [expandedMetrics, setExpandedMetrics] = useState(null); // userId con panel abierto

  const [editEspecialidad, setEditEspecialidad] = useState(null);
  const [especialidadValue, setEspecialidadValue] = useState('');

  const [editRol, setEditRol] = useState(null);
  const [rolValue, setRolValue] = useState('');

  const [confirmDelete, setConfirmDelete] = useState(null);

  // ─── Carga ────────────────────────────────────────────────────
  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [medicosRes, adminsRes] = await Promise.all([
        api.get('/users/medicos'),
        api.get('/users/role/ADMIN'),
      ]);
      const merged = [...medicosRes.data, ...adminsRes.data];
      const unique = merged.filter(
        (u, i, arr) => arr.findIndex(x => x.id === u.id) === i
      );
      setUsers(unique);
    } catch {
      setError('No se pudieron cargar los usuarios. Verifica que el servidor esté activo.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  // ─── Utilidades ───────────────────────────────────────────────
  const adminCount = users.filter(u => u.role === 'ADMIN').length;

  const flash = (type, msg) => {
    if (type === 'success') { setSuccess(msg); setTimeout(() => setSuccess(''), 3500); }
    else                    { setError(msg);   setTimeout(() => setError(''),   3500); }
  };

  const toggleMetrics = (userId) => {
    setExpandedMetrics(prev => prev === userId ? null : userId);
  };

  // ─── Guardar especialidad ─────────────────────────────────────
  const handleGuardarEspecialidad = async (userId) => {
    const user = users.find(u => u.id === userId);
    try {
      await api.put(`/users/${userId}`, { ...user, especialidad: especialidadValue || null });
      flash('success', 'Especialidad actualizada correctamente.');
      setEditEspecialidad(null);
      fetchUsers();
    } catch {
      flash('error', 'Error al actualizar la especialidad.');
    }
  };

  // ─── Guardar rol ──────────────────────────────────────────────
  const handleGuardarRol = async (userId) => {
    const user = users.find(u => u.id === userId);
    if (user.role === 'ADMIN' && rolValue !== 'ADMIN' && adminCount <= 1) {
      flash('error', 'No puedes cambiar el rol: debe existir al menos un administrador en el sistema.');
      setEditRol(null);
      return;
    }
    try {
      await api.put(`/users/${userId}`, {
        ...user,
        role:         rolValue,
        especialidad: rolValue === 'MEDICO' ? user.especialidad : null,
      });
      flash('success', 'Rol actualizado correctamente.');
      setEditRol(null);
      fetchUsers();
    } catch {
      flash('error', 'Error al actualizar el rol.');
    }
  };

  // ─── Eliminar ─────────────────────────────────────────────────
  const handleEliminar = async (userId) => {
    const user = users.find(u => u.id === userId);
    if (user.role === 'ADMIN' && adminCount <= 1) {
      flash('error', 'No puedes eliminar al único administrador del sistema.');
      setConfirmDelete(null);
      return;
    }
    try {
      await api.delete(`/users/${userId}`);
      flash('success', `Usuario ${user.name} ${user.lastname} eliminado correctamente.`);
      setConfirmDelete(null);
      if (expandedMetrics === userId) setExpandedMetrics(null);
      fetchUsers();
    } catch {
      flash('error', 'Error al eliminar el usuario.');
    }
  };

  // ─── Render ───────────────────────────────────────────────────
  if (loading) return <p className="text-gray-500 animate-pulse">Cargando usuarios...</p>;

  return (
    <div>
      {error   && <p className="text-red-600 bg-red-50   border border-red-200   rounded-lg p-3 text-sm mb-4">{error}</p>}
      {success && <p className="text-green-700 bg-green-50 border border-green-200 rounded-lg p-3 text-sm mb-4">{success}</p>}

      {consultasMedico && (
        <ConsultasModal
          medico={consultasMedico}
          onClose={() => setConsultasMedico(null)}
        />
      )}

      <p className="text-xs text-gray-500 mb-3">{users.length} usuario(s) encontrado(s)</p>

      <div className="overflow-x-auto">
        <table className="w-full text-sm border border-gray-200 rounded-lg overflow-hidden">
          <thead className="bg-blue-700 text-white">
            <tr>
              <th className="px-4 py-3 text-left">Nombre</th>
              <th className="px-4 py-3 text-left">RUT</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Rol</th>
              <th className="px-4 py-3 text-left">Especialidad</th>
              <th className="px-4 py-3 text-left">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  No hay médicos ni administradores registrados.
                </td>
              </tr>
            )}

            {users.map(user => {
              const esUltimoAdmin  = user.role === 'ADMIN' && adminCount <= 1;
              const metricsAbierto = expandedMetrics === user.id;

              return (
                <>
                  <tr
                    key={user.id}
                    className={`border-t border-gray-100 align-middle transition-colors ${metricsAbierto ? 'bg-blue-50' : 'hover:bg-gray-50'}`}
                  >
                    {/* Nombre */}
                    <td className="px-4 py-3 font-medium">
                      {user.name} {user.lastname}
                    </td>

                    {/* RUT */}
                    <td className="px-4 py-3 text-gray-500">
                      {user.rut}-{user.dv}
                    </td>

                    {/* Email */}
                    <td className="px-4 py-3 text-gray-600">{user.email}</td>

                    {/* ROL */}
                    <td className="px-4 py-3">
                      {editRol === user.id ? (
                        <div className="flex gap-1 items-center">
                          <select
                            value={rolValue}
                            onChange={e => setRolValue(e.target.value)}
                            className="border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
                          >
                            {ROLES_DISPONIBLES.map(r => (
                              <option key={r} value={r}>{r}</option>
                            ))}
                          </select>
                          <button onClick={() => handleGuardarRol(user.id)} className="text-green-600 font-bold text-xs px-1 hover:text-green-800" title="Guardar">✓</button>
                          <button onClick={() => setEditRol(null)} className="text-red-500 text-xs px-1 hover:text-red-700" title="Cancelar">✕</button>
                        </div>
                      ) : esUltimoAdmin ? (
                        <div className="flex items-center gap-1.5">
                          {rolBadge(user.role)}
                          <span className="text-xs text-gray-400 cursor-not-allowed" title="No se puede cambiar el rol: es el único administrador del sistema">!!</span>
                        </div>
                      ) : (
                        <button onClick={() => { setEditRol(user.id); setRolValue(user.role); }} className="hover:opacity-75 transition-opacity" title="Clic para cambiar rol">
                          {rolBadge(user.role)}
                        </button>
                      )}
                    </td>

                    {/* ESPECIALIDAD */}
                    <td className="px-4 py-3">
                      {user.role === 'MEDICO' ? (
                        editEspecialidad === user.id ? (
                          <div className="flex gap-1 items-center">
                            <select
                              value={especialidadValue}
                              onChange={e => setEspecialidadValue(e.target.value)}
                              className="border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
                            >
                              <option value="">Sin especialidad</option>
                              {ESPECIALIDADES.map(esp => (
                                <option key={esp} value={esp}>{esp}</option>
                              ))}
                            </select>
                            <button onClick={() => handleGuardarEspecialidad(user.id)} className="text-green-600 font-bold text-xs px-1 hover:text-green-800" title="Guardar">✓</button>
                            <button onClick={() => setEditEspecialidad(null)} className="text-red-500 text-xs px-1 hover:text-red-700" title="Cancelar">✕</button>
                          </div>
                        ) : (
                          <button
                            onClick={() => { setEditEspecialidad(user.id); setEspecialidadValue(user.especialidad ?? ''); }}
                            className="text-blue-600 hover:underline text-sm text-left"
                            title="Clic para editar especialidad"
                          >
                            {user.especialidad ?? <span className="text-gray-400 italic">Sin especialidad</span>}
                          </button>
                        )
                      ) : (
                        <span className="text-gray-300">—</span>
                      )}
                    </td>

                    {/* ACCIONES */}
                    <td className="px-4 py-3">
                      {confirmDelete === user.id ? (
                        <div className="flex gap-1 items-center">
                          <span className="text-xs text-red-600 mr-1">¿Eliminar?</span>
                          <button onClick={() => handleEliminar(user.id)} className="text-xs bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700">Sí</button>
                          <button onClick={() => setConfirmDelete(null)} className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 px-2 py-1 rounded">No</button>
                        </div>
                      ) : (
                        <div className="flex gap-2 flex-wrap">
                          {user.role === 'MEDICO' && (
                            <>
                              <button
                                onClick={() => toggleMetrics(user.id)}
                                className={`text-xs px-2 py-1 rounded transition-colors ${
                                  metricsAbierto
                                    ? 'bg-blue-700 text-white'
                                    : 'bg-blue-100 hover:bg-blue-200 text-blue-700'
                                }`}
                              >
                                {metricsAbierto ? '▲ Métricas' : '▼ Métricas'}
                              </button>
                              <button
                                onClick={() => setConsultasMedico(user)}
                                className="text-xs bg-indigo-100 hover:bg-indigo-200 text-indigo-700 px-2 py-1 rounded"
                              >
                                Ver consultas
                              </button>
                            </>
                          )}
                          <button
                            onClick={() => setConfirmDelete(user.id)}
                            disabled={esUltimoAdmin}
                            title={esUltimoAdmin ? 'No puedes eliminar al único administrador' : 'Eliminar usuario'}
                            className={`text-xs px-2 py-1 rounded transition-colors ${
                              esUltimoAdmin
                                ? 'bg-gray-100 text-gray-300 cursor-not-allowed'
                                : 'bg-red-100 hover:bg-red-200 text-red-700'
                            }`}
                          >
                            Eliminar
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>

                  {/* Panel de métricas expandible */}
                  {metricsAbierto && user.role === 'MEDICO' && (
                    <MetricasPanel key={`metrics-${user.id}`} medico={user} />
                  )}
                </>
              );
            })}
          </tbody>
        </table>
      </div>

      <p className="text-xs text-gray-400 mt-3">
        Haz clic en el <strong>badge de rol</strong> o en la <strong>especialidad</strong> para editarlos directamente en la tabla.
      </p>
    </div>
  );
};

export default MedicosAdminsTab;