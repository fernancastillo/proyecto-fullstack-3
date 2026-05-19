import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axios';

const REGIONES_CHILE = [
  'Arica y Parinacota','Tarapacá','Antofagasta','Atacama','Coquimbo',
  'Valparaíso','Metropolitana de Santiago','O\'Higgins','Maule','Ñuble',
  'Biobío','La Araucanía','Los Ríos','Los Lagos','Aysén','Magallanes',
];

const PacientesTab = () => {
  const [pacientes, setPacientes] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');
  const [success, setSuccess]     = useState('');
  const [busqueda, setBusqueda]   = useState('');

  const [editUser, setEditUser]   = useState(null);
  const [editForm, setEditForm]   = useState({});
  const [saving, setSaving]       = useState(false);

  const flash = (type, msg) => {
    if (type === 'success') { setSuccess(msg); setTimeout(() => setSuccess(''), 3500); }
    else                    { setError(msg);   setTimeout(() => setError(''),   3500); }
  };

  const fetchPacientes = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/users/role/PACIENTE');
      setPacientes(res.data);
    } catch {
      setError('No se pudieron cargar los pacientes. Verifica que el servidor esté activo.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchPacientes(); }, [fetchPacientes]);

  const termino   = busqueda.toLowerCase().trim();
  const filtrados = pacientes.filter(p =>
    !termino ||
    p.name.toLowerCase().includes(termino)     ||
    p.lastname.toLowerCase().includes(termino) ||
    p.rut.includes(termino)                    ||
    p.region?.toLowerCase().includes(termino)  ||
    p.comuna?.toLowerCase().includes(termino)
  );

  const openEdit = (p) => {
    setEditUser(p);
    setEditForm({
      name:     p.name,
      lastname: p.lastname,
      email:    p.email,
      phone:    p.phone,
      region:   p.region,
      comuna:   p.comuna,
      address:  p.address,
    });
  };

  const handleSaveEdit = async () => {
    setSaving(true);
    try {
      await api.put(`/users/${editUser.id}`, { ...editUser, ...editForm });
      flash('success', `Paciente ${editForm.name} ${editForm.lastname} actualizado correctamente.`);
      setEditUser(null);
      fetchPacientes();
    } catch {
      flash('error', 'Error al actualizar el paciente.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p className="text-gray-500 animate-pulse">Cargando pacientes...</p>;

  return (
    <div>
      {error   && <p className="text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 text-sm mb-4">{error}</p>}
      {success && <p className="text-green-700 bg-green-50 border border-green-200 rounded-lg p-3 text-sm mb-4">{success}</p>}

      {/* Modal de edición */}
      {editUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 p-6">
            <h2 className="text-lg font-bold text-blue-700 mb-4">
              Editar Paciente — {editUser.name} {editUser.lastname}
            </h2>

            <div className="grid grid-cols-2 gap-3 text-sm">
              {[
                { label: 'Nombre',    key: 'name' },
                { label: 'Apellido',  key: 'lastname' },
                { label: 'Email',     key: 'email' },
                { label: 'Teléfono', key: 'phone' },
                { label: 'Dirección', key: 'address' },
                { label: 'Comuna',    key: 'comuna' },
              ].map(({ label, key }) => (
                <div key={key} className={key === 'address' ? 'col-span-2' : ''}>
                  <label className="block text-xs text-gray-500 mb-1">{label}</label>
                  <input
                    type="text"
                    value={editForm[key] ?? ''}
                    onChange={e => setEditForm(f => ({ ...f, [key]: e.target.value }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-400"
                  />
                </div>
              ))}

              <div>
                <label className="block text-xs text-gray-500 mb-1">Región</label>
                <select
                  value={editForm.region ?? ''}
                  onChange={e => setEditForm(f => ({ ...f, region: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-400"
                >
                  {REGIONES_CHILE.map(r => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-2 mt-5">
              <button
                onClick={() => setEditUser(null)}
                className="px-4 py-2 text-sm rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50"
              >
                Cancelar
              </button>
              <button
                onClick={handleSaveEdit}
                disabled={saving}
                className="px-4 py-2 text-sm rounded-lg bg-blue-700 text-white hover:bg-blue-800 disabled:opacity-50"
              >
                {saving ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Buscador */}
      <div className="mb-4 flex items-center gap-3">
        <input
          type="text"
          placeholder="Buscar por nombre, apellido, RUT, región o comuna..."
          value={busqueda}
          onChange={e => setBusqueda(e.target.value)}
          className="border border-gray-300 rounded-lg px-4 py-2 w-full max-w-md text-sm
                     focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {busqueda && (
          <button
            onClick={() => setBusqueda('')}
            className="text-xs text-gray-500 hover:text-gray-700"
          >
            Limpiar
          </button>
        )}
      </div>

      <p className="text-xs text-gray-500 mb-3">
        {filtrados.length} paciente(s)
        {busqueda ? ` que coinciden con "${busqueda}"` : ' registrados en el sistema'}
      </p>

      <div className="overflow-x-auto">
        <table className="w-full text-sm border border-gray-200 rounded-lg overflow-hidden">
          <thead className="bg-blue-700 text-white">
            <tr>
              <th className="px-4 py-3 text-left">Nombre y Apellido</th>
              <th className="px-4 py-3 text-left">RUT</th>
              <th className="px-4 py-3 text-left">Teléfono</th>
              <th className="px-4 py-3 text-left">Región / Comuna</th>
              <th className="px-4 py-3 text-left">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filtrados.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                  {busqueda
                    ? `No se encontraron pacientes para "${busqueda}".`
                    : 'No hay pacientes registrados en el sistema.'}
                </td>
              </tr>
            ) : (
              filtrados.map(p => (
                <tr key={p.id} className="border-t border-gray-100 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{p.name} {p.lastname}</td>
                  <td className="px-4 py-3 text-gray-500">{p.rut}-{p.dv}</td>
                  <td className="px-4 py-3 text-gray-600">{p.phone}</td>
                  <td className="px-4 py-3 text-gray-600">{p.region} / {p.comuna}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => openEdit(p)}
                      className="text-xs bg-blue-100 hover:bg-blue-200 text-blue-700 px-2 py-1 rounded transition-colors"
                    >
                      Editar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PacientesTab;