import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const ESTADOS = {
  PENDIENTE:  'bg-yellow-100 text-yellow-800',
  CONFIRMADA: 'bg-green-100 text-green-800',
  CANCELADA:  'bg-red-100 text-red-800',
};

const ESPECIALIDADES = [
  'Cardiología', 'Pediatría', 'Neurología',
  'Traumatología', 'Medicina General', 'Dermatología', 'Ginecología',
];

const RequestsPage = () => {
  const { user } = useAuth();

  const [requests, setRequests]       = useState([]);
  const [medicos, setMedicos]         = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState('');
  const [success, setSuccess]         = useState('');
  const [showForm, setShowForm]       = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formErrors, setFormErrors]   = useState({});

  const emptyForm = { especialidad: '', descripcion: '', medicoId: '' };
  const [formData, setFormData] = useState(emptyForm);

  // ─── Carga inicial ───────────────────────────────────────────
  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [reqRes, medRes] = await Promise.all([
        api.get(`/requests/user/${user.id}`),
        api.get('/users/medicos'),
      ]);
      setRequests(reqRes.data);
      setMedicos(medRes.data);
    } catch {
      setError('No se pudieron cargar las solicitudes. Verifica que el servidor esté activo.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { if (user?.id) fetchData(); }, [user]);

  // Filtrar médicos por especialidad seleccionada
  const medicosFiltrados = medicos.filter(
    m => !formData.especialidad || m.especialidad === formData.especialidad
  );

  // ─── Validación ─────────────────────────────────────────────
  const validate = () => {
    const newErrors = {};
    if (!formData.especialidad) newErrors.especialidad = 'Selecciona una especialidad.';
    if (!formData.medicoId)     newErrors.medicoId     = 'Selecciona un médico.';
    if (!formData.descripcion.trim()) newErrors.descripcion = 'La descripción es obligatoria.';
    return newErrors;
  };

  // ─── Handlers ───────────────────────────────────────────────
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Al cambiar especialidad, resetear médico
    if (name === 'especialidad') setFormData(prev => ({ ...prev, especialidad: value, medicoId: '' }));
    if (formErrors[name]) setFormErrors(prev => ({ ...prev, [name]: '' }));
  };

  const showSuccessMsg = (msg) => {
    setSuccess(msg);
    setTimeout(() => setSuccess(''), 3000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      return;
    }

    setFormLoading(true);
    try {
      await api.post('/requests', {
        userId:      user.id,
        medicoId:    parseInt(formData.medicoId),
        especialidad: formData.especialidad,
        descripcion:  formData.descripcion,
        estado:       'PENDIENTE',
      });
      setFormData(emptyForm);
      setFormErrors({});
      setShowForm(false);
      showSuccessMsg('Solicitud creada correctamente.');
      fetchData();
    } catch {
      setFormErrors({ general: 'No se pudo guardar la solicitud. Intenta nuevamente.' });
    } finally {
      setFormLoading(false);
    }
  };

  // ─── Subcomponentes ─────────────────────────────────────────
  const ErrorMsg = ({ msg }) =>
    msg ? (
      <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1">
        <svg className="w-3.5 h-3.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
        </svg>
        {msg}
      </p>
    ) : null;

  const selectBase = 'w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm bg-slate-50 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-colors duration-150';
  const inputBase  = 'w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm placeholder-slate-300 bg-slate-50 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-colors duration-150';

  // ─── Render ─────────────────────────────────────────────────
  return (
    <div className="max-w-5xl mx-auto">

      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Mis Solicitudes</h1>
          <p className="text-sm text-slate-500 mt-0.5">Gestiona tus solicitudes de atención médica</p>
        </div>
        <button
          onClick={() => { setShowForm(true); setFormData(emptyForm); setFormErrors({}); }}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          Nueva solicitud
        </button>
      </div>

      {/* Success banner */}
      {success && (
        <div className="mb-4 flex items-center gap-3 bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl">
          <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {success}
        </div>
      )}

      {/* Error banner */}
      {error && (
        <div className="mb-4 flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {error}
        </div>
      )}

      {/* Tabla */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center py-20 text-slate-400">
            <svg className="w-6 h-6 animate-spin mr-2" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Cargando solicitudes...
          </div>
        ) : requests.length === 0 ? (
          <div className="text-center py-20 text-slate-400">
            <svg className="w-10 h-10 mx-auto mb-3 opacity-40" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="text-sm">No tienes solicitudes registradas.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  {['#', 'Especialidad', 'Médico', 'Descripción', 'Estado', 'Fecha'].map(h => (
                    <th key={h} className="text-left text-xs font-medium text-slate-500 uppercase tracking-wider px-5 py-3.5">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {requests.map(req => {
                  const medico = medicos.find(m => m.id === req.medicoId);
                  return (
                    <tr key={req.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-5 py-3.5 text-slate-400 font-mono text-xs">#{req.id}</td>
                      <td className="px-5 py-3.5 text-slate-800 font-medium">{req.especialidad}</td>
                      <td className="px-5 py-3.5 text-slate-600">
                        {medico ? `${medico.name} ${medico.lastname}` : `ID: ${req.medicoId}`}
                      </td>
                      <td className="px-5 py-3.5 text-slate-600 max-w-xs truncate">{req.descripcion}</td>
                      <td className="px-5 py-3.5">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${ESTADOS[req.estado] || 'bg-slate-100 text-slate-600'}`}>
                          {req.estado}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-slate-500 text-xs">
                        {req.fechaSolicitud ? new Date(req.fechaSolicitud).toLocaleDateString('es-CL') : '—'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal formulario */}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md">
            <div className="flex items-center justify-between px-8 py-5 border-b border-slate-100">
              <h2 className="text-lg font-semibold text-slate-800">Nueva solicitud</h2>
              <button
                onClick={() => setShowForm(false)}
                className="text-slate-400 hover:text-slate-600 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmit} noValidate className="px-8 py-6 space-y-5">
              {formErrors.general && (
                <div className="flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
                  {formErrors.general}
                </div>
              )}

              {/* Especialidad */}
              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                  Especialidad
                </label>
                <select
                  name="especialidad"
                  value={formData.especialidad}
                  onChange={handleChange}
                  className={`${selectBase} ${formErrors.especialidad ? 'border-red-400' : 'border-slate-200'}`}
                >
                  <option value="">Selecciona una especialidad...</option>
                  {ESPECIALIDADES.map(esp => (
                    <option key={esp} value={esp}>{esp}</option>
                  ))}
                </select>
                <ErrorMsg msg={formErrors.especialidad} />
              </div>

              {/* Médico — se filtra por especialidad */}
              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                  Médico
                </label>
                <select
                  name="medicoId"
                  value={formData.medicoId}
                  onChange={handleChange}
                  disabled={!formData.especialidad}
                  className={`${selectBase} ${formErrors.medicoId ? 'border-red-400' : 'border-slate-200'} disabled:opacity-50`}
                >
                  <option value="">
                    {formData.especialidad ? 'Selecciona un médico...' : 'Primero selecciona una especialidad'}
                  </option>
                  {medicosFiltrados.map(m => (
                    <option key={m.id} value={m.id}>
                      {m.name} {m.lastname} — {m.especialidad}
                    </option>
                  ))}
                </select>
                <ErrorMsg msg={formErrors.medicoId} />
              </div>

              {/* Descripción */}
              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                  Descripción
                </label>
                <textarea
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleChange}
                  placeholder="Describe brevemente el motivo de la consulta..."
                  rows={3}
                  className={`${inputBase} resize-none ${formErrors.descripcion ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
                />
                <ErrorMsg msg={formErrors.descripcion} />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="px-4 py-2.5 text-sm font-medium text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={formLoading}
                  className="px-4 py-2.5 text-sm font-medium bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded-lg transition-colors flex items-center gap-2"
                >
                  {formLoading ? (
                    <>
                      <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                      </svg>
                      Guardando...
                    </>
                  ) : 'Crear solicitud'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default RequestsPage;