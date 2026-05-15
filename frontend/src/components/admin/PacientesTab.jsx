import { useState, useEffect } from 'react';
import api from '../../api/axios';

const PacientesTab = () => {
  const [pacientes, setPacientes] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');
  const [busqueda, setBusqueda]   = useState('');

  useEffect(() => {
    api.get('/users/role/PACIENTE')
      .then(res => setPacientes(res.data))
      .catch(() => setError('No se pudieron cargar los pacientes. Verifica que el servidor esté activo.'))
      .finally(() => setLoading(false));
  }, []);

  const termino = busqueda.toLowerCase().trim();

  const filtrados = pacientes.filter(p =>
    !termino ||
    p.name.toLowerCase().includes(termino)     ||
    p.lastname.toLowerCase().includes(termino) ||
    p.rut.includes(termino)                    ||
    p.region?.toLowerCase().includes(termino)  ||
    p.comuna?.toLowerCase().includes(termino)
  );

  if (loading) return <p className="text-gray-500 animate-pulse">Cargando pacientes...</p>;

  return (
    <div>
      {error && (
        <p className="text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 text-sm mb-4">
          {error}
        </p>
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
            </tr>
          </thead>
          <tbody>
            {filtrados.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
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