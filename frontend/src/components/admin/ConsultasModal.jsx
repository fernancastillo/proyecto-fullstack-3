import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axios';

const ESTADOS = ['PENDIENTE', 'CONFIRMADA', 'CANCELADA'];

const estadoBadge = (estado) => {
  const clases = {
    CONFIRMADA: 'bg-green-100 text-green-700',
    CANCELADA:  'bg-red-100 text-red-700',
    PENDIENTE:  'bg-yellow-100 text-yellow-700',
  };
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${clases[estado] ?? 'bg-gray-100 text-gray-600'}`}>
      {estado}
    </span>
  );
};

const formatFecha = (fecha) => {
  if (!fecha) return '—';
  return new Date(fecha).toLocaleString('es-CL', {
    dateStyle: 'short',
    timeStyle: 'short',
  });
};

/**
 * Convierte un LocalDateTime de Java (array o string ISO) a string
 * apto para <input type="datetime-local"> (YYYY-MM-DDTHH:MM).
 */
const toInputDateTime = (fecha) => {
  if (!fecha) return '';
  const d = new Date(fecha);
  if (isNaN(d)) return '';
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const ConsultasModal = ({ medico, onClose }) => {
  const [consultas, setConsultas] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');
  const [success, setSuccess]     = useState('');

  // ID de la consulta en modo edición
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm]   = useState({ fechaCita: '', estado: '' });

  // ID de la consulta esperando confirmación de borrado
  const [confirmDelete, setConfirmDelete] = useState(null);

  const fetchConsultas = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/requests/medico/${medico.id}`);
      setConsultas(res.data);
    } catch {
      setError('No se pudieron cargar las consultas de este médico.');
    } finally {
      setLoading(false);
    }
  }, [medico.id]);

  useEffect(() => { fetchConsultas(); }, [fetchConsultas]);

  // ─── Editar ───────────────────────────────────────────────────
  const handleStartEdit = (c) => {
    setEditingId(c.id);
    setEditForm({ fechaCita: toInputDateTime(c.fechaCita), estado: c.estado });
  };

    const handleSaveEdit = async (consulta) => {
    try {
        await api.put(`/requests/${consulta.id}`, {
        userId:         consulta.userId,
        medicoId:       consulta.medicoId,
        especialidad:   consulta.especialidad,
        descripcion:    consulta.descripcion,
        estado:         editForm.estado,
        fechaSolicitud: consulta.fechaSolicitud,
        fechaCita:      editForm.fechaCita ? editForm.fechaCita + ':00' : null,
        });
        flash('success', 'Consulta actualizada correctamente.');
        setEditingId(null);
        fetchConsultas();
    } catch {
        flash('error', 'Error al actualizar la consulta.');
    }
    };

  // ─── Eliminar ─────────────────────────────────────────────────
  const handleDelete = async (id) => {
    try {
      await api.delete(`/requests/${id}`);
      flash('success', 'Consulta eliminada correctamente.');
      setConfirmDelete(null);
      fetchConsultas();
    } catch {
      flash('error', 'Error al eliminar la consulta.');
    }
  };

  // ─── Util ─────────────────────────────────────────────────────
  const flash = (type, msg) => {
    if (type === 'success') { setSuccess(msg); setTimeout(() => setSuccess(''), 3500); }
    else                    { setError(msg);   setTimeout(() => setError(''),   3500); }
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col">

        {/* Cabecera */}
        <div className="flex justify-between items-start p-5 border-b border-gray-200">
          <div>
            <h2 className="text-lg font-bold text-blue-700">
              Consultas de {medico.name} {medico.lastname}
            </h2>
            <p className="text-sm text-gray-500 mt-0.5">
              {medico.especialidad ?? 'Sin especialidad asignada'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-700 text-2xl leading-none ml-4"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>

        {/* Alertas */}
        <div className="px-5 pt-4">
          {error   && <p className="text-red-600 bg-red-50   border border-red-200   rounded-lg p-3 text-sm mb-2">{error}</p>}
          {success && <p className="text-green-700 bg-green-50 border border-green-200 rounded-lg p-3 text-sm mb-2">{success}</p>}
        </div>

        {/* Contenido */}
        <div className="p-5 overflow-y-auto flex-1">
          {loading ? (
            <p className="text-gray-500 animate-pulse">Cargando consultas...</p>
          ) : consultas.length === 0 ? (
            <div className="text-center py-10 text-gray-400">
              <p className="text-4xl mb-2"></p>
              <p>Este médico no tiene consultas asignadas.</p>
            </div>
          ) : (
            <>
              <p className="text-xs text-gray-500 mb-3">{consultas.length} consulta(s) encontrada(s)</p>
              <div className="overflow-x-auto">
                <table className="w-full text-sm border border-gray-200 rounded-lg overflow-hidden">
                  <thead className="bg-gray-100 text-gray-700">
                    <tr>
                      <th className="px-3 py-2 text-left">ID</th>
                      <th className="px-3 py-2 text-left">Especialidad</th>
                      <th className="px-3 py-2 text-left">Descripción</th>
                      <th className="px-3 py-2 text-left">Solicitada</th>
                      <th className="px-3 py-2 text-left">Fecha Cita</th>
                      <th className="px-3 py-2 text-left">Estado</th>
                      <th className="px-3 py-2 text-left">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {consultas.map(c => (
                      <tr key={c.id} className="border-t border-gray-100 hover:bg-gray-50 align-top">
                        <td className="px-3 py-3 text-gray-400">#{c.id}</td>
                        <td className="px-3 py-3">{c.especialidad}</td>
                        <td className="px-3 py-3 max-w-[180px]">
                          <p className="truncate" title={c.descripcion}>{c.descripcion}</p>
                        </td>
                        <td className="px-3 py-3 text-gray-500 whitespace-nowrap">
                          {formatFecha(c.fechaSolicitud)}
                        </td>

                        {/* Fecha cita — editable */}
                        <td className="px-3 py-3 whitespace-nowrap">
                          {editingId === c.id ? (
                            <input
                              type="datetime-local"
                              value={editForm.fechaCita}
                              onChange={e => setEditForm(f => ({ ...f, fechaCita: e.target.value }))}
                              className="border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
                            />
                          ) : (
                            formatFecha(c.fechaCita)
                          )}
                        </td>

                        {/* Estado — editable */}
                        <td className="px-3 py-3">
                          {editingId === c.id ? (
                            <select
                              value={editForm.estado}
                              onChange={e => setEditForm(f => ({ ...f, estado: e.target.value }))}
                              className="border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
                            >
                              {ESTADOS.map(est => (
                                <option key={est} value={est}>{est}</option>
                              ))}
                            </select>
                          ) : (
                            estadoBadge(c.estado)
                          )}
                        </td>

                        {/* Acciones */}
                        <td className="px-3 py-3">
                          {editingId === c.id ? (
                            <div className="flex gap-1">
                              <button
                                onClick={() => handleSaveEdit(c)}
                                className="text-xs bg-green-100 hover:bg-green-200 text-green-700 px-2 py-1 rounded font-medium"
                              >
                                Guardar
                              </button>
                              <button
                                onClick={() => setEditingId(null)}
                                className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 px-2 py-1 rounded"
                              >
                                Cancelar
                              </button>
                            </div>
                          ) : confirmDelete === c.id ? (
                            <div className="flex gap-1 items-center">
                              <span className="text-xs text-red-600 mr-1">¿Confirmar?</span>
                              <button
                                onClick={() => handleDelete(c.id)}
                                className="text-xs bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                              >
                                Sí
                              </button>
                              <button
                                onClick={() => setConfirmDelete(null)}
                                className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 px-2 py-1 rounded"
                              >
                                No
                              </button>
                            </div>
                          ) : (
                            <div className="flex gap-1">
                              <button
                                onClick={() => handleStartEdit(c)}
                                className="text-xs bg-blue-100 hover:bg-blue-200 text-blue-700 px-2 py-1 rounded"
                              >
                                Editar
                              </button>
                              <button
                                onClick={() => setConfirmDelete(c.id)}
                                className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded"
                              >
                                Eliminar
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ConsultasModal;