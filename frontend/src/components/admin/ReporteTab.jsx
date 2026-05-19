import { useState, useMemo } from 'react';
import api from '../../api/axios';

// ─── Helpers ──────────────────────────────────────────────────────────────────

const formatFecha = (fecha) => {
  if (!fecha) return '—';
  return new Date(fecha).toLocaleString('es-CL', {
    day:    '2-digit',
    month:  '2-digit',
    year:   'numeric',
    hour:   '2-digit',
    minute: '2-digit',
  });
};

const exportarCSV = (logs) => {
  const encabezados = ['ID', 'Microservicio', 'Endpoint', 'Método HTTP', 'Status', 'Tiempo (ms)', 'Fecha', 'Error'];
  const filas = logs.map(log => [
    log.id,
    log.microservicio,
    log.endpoint,
    log.metodoHttp,
    log.status,
    log.tiempoRespuesta,
    log.fecha ? new Date(log.fecha).toLocaleString('es-CL') : '',
    log.errorMensaje ?? '',
  ]);

  const contenido = [encabezados, ...filas]
    .map(fila => fila.map(celda => `"${String(celda).replace(/"/g, '""')}"`).join(','))
    .join('\n');

  const blob = new Blob(['\uFEFF' + contenido], { type: 'text/csv;charset=utf-8;' });
  const url  = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href     = url;
  link.download = `reporte-logs.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

const METODOS_HTTP = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];

const SelectFiltro = ({ label, value, onChange, options, placeholder }) => (
  <div className="flex flex-col gap-1">
    <label className="text-xs font-medium text-gray-600">{label}</label>
    <select
      value={value}
      onChange={e => onChange(e.target.value)}
      className="border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white
                 focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <option value="">{placeholder}</option>
      {options.map(o => <option key={o} value={o}>{o}</option>)}
    </select>
  </div>
);

// ─── Componente ───────────────────────────────────────────────────────────────

const ReporteTab = () => {
  const hoy       = new Date().toISOString().slice(0, 10);
  const hace7dias = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);

  // Búsqueda
  const [fechaInicio, setFechaInicio] = useState(hace7dias);
  const [fechaFin,    setFechaFin]    = useState(hoy);
  const [logs,        setLogs]        = useState([]);
  const [loading,     setLoading]     = useState(false);
  const [error,       setError]       = useState('');
  const [buscado,     setBuscado]     = useState(false);

  // Filtros (se aplican sobre los datos ya cargados)
  const [filtroMicroservicio, setFiltroMicroservicio] = useState('');
  const [filtroMetodo,        setFiltroMetodo]        = useState('');
  const [filtroStatus,        setFiltroStatus]        = useState('');
  const [filtroSoloErrores,   setFiltroSoloErrores]   = useState(false);
  const [filtroEndpoint,      setFiltroEndpoint]      = useState('');

  const generarReporte = () => {
    if (!fechaInicio || !fechaFin) {
      setError('Debes seleccionar ambas fechas.');
      return;
    }
    if (fechaInicio > fechaFin) {
      setError('La fecha de inicio no puede ser posterior a la fecha de fin.');
      return;
    }

    setLoading(true);
    setError('');
    setLogs([]);
    setBuscado(false);
    // Limpiar filtros al nueva búsqueda
    setFiltroMicroservicio('');
    setFiltroMetodo('');
    setFiltroStatus('');
    setFiltroSoloErrores(false);
    setFiltroEndpoint('');

    api.get('/logs/rango', { params: { inicio: fechaInicio, fin: fechaFin } })
      .then(res => { setLogs(res.data); setBuscado(true); })
      .catch(() => setError('No se pudo obtener el reporte. Verifica que el servidor esté activo.'))
      .finally(() => setLoading(false));
  };

  // Opciones dinámicas extraídas de los datos reales
  const microservicios = useMemo(() => [...new Set(logs.map(l => l.microservicio).filter(Boolean))].sort(), [logs]);
  const statusCodes    = useMemo(() => [...new Set(logs.map(l => Number(l.status)).filter(Boolean))].sort((a, b) => a - b), [logs]);

  // Aplicar filtros
  const logsFiltrados = useMemo(() => {
    return logs.filter(log => {
      if (filtroMicroservicio && log.microservicio !== filtroMicroservicio)                            return false;
      if (filtroMetodo        && log.metodoHttp    !== filtroMetodo)                                   return false;
      if (filtroStatus        && Number(log.status) !== Number(filtroStatus))                          return false;
      if (filtroSoloErrores   && !log.errorMensaje)                                                    return false;
      if (filtroEndpoint      && !log.endpoint?.toLowerCase().includes(filtroEndpoint.toLowerCase()))  return false;
      return true;
    });
  }, [logs, filtroMicroservicio, filtroMetodo, filtroStatus, filtroSoloErrores, filtroEndpoint]);

  const hayFiltrosActivos = filtroMicroservicio || filtroMetodo || filtroStatus || filtroSoloErrores || filtroEndpoint;

  const limpiarFiltros = () => {
    setFiltroMicroservicio('');
    setFiltroMetodo('');
    setFiltroStatus('');
    setFiltroSoloErrores(false);
    setFiltroEndpoint('');
  };

  return (
    <div>
      <h2 className="text-base font-semibold text-gray-700 mb-4">Reporte de Logs del Sistema</h2>

      {/* ── Selector de rango de fechas ── */}
      <div className="bg-gray-50 border border-gray-200 rounded-xl p-5 mb-4">
        <p className="text-sm text-gray-500 mb-4">
          Selecciona el rango de fechas para generar el reporte de logs registrados en todos los microservicios.
        </p>

        <div className="flex flex-wrap items-end gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium text-gray-600">Fecha inicio</label>
            <input
              type="date"
              value={fechaInicio}
              max={fechaFin || hoy}
              onChange={e => setFechaInicio(e.target.value)}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium text-gray-600">Fecha fin</label>
            <input
              type="date"
              value={fechaFin}
              min={fechaInicio}
              max={hoy}
              onChange={e => setFechaFin(e.target.value)}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            onClick={generarReporte}
            disabled={loading}
            className="px-5 py-2 bg-blue-700 text-white text-sm font-medium rounded-lg
                       hover:bg-blue-800 disabled:opacity-50 disabled:cursor-not-allowed
                       transition-colors"
          >
            {loading ? 'Buscando...' : 'Generar reporte'}
          </button>

          {buscado && logsFiltrados.length > 0 && (
            <button
              onClick={() => exportarCSV(logsFiltrados)}
              className="px-5 py-2 bg-green-600 text-white text-sm font-medium rounded-lg
                         hover:bg-green-700 transition-colors"
            >
              Exportar CSV
            </button>
          )}
        </div>
      </div>

      {/* ── Filtros (solo visibles tras cargar datos) ── */}
      {buscado && logs.length > 0 && (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-5 mb-4">
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Filtrar resultados</p>
            {hayFiltrosActivos && (
              <button
                onClick={limpiarFiltros}
                className="text-xs text-blue-600 hover:text-blue-800"
              >
                Limpiar filtros
              </button>
            )}
          </div>

          <div className="flex flex-wrap gap-4 items-end">
            <SelectFiltro
              label="Microservicio"
              value={filtroMicroservicio}
              onChange={setFiltroMicroservicio}
              options={microservicios}
              placeholder="Todos"
            />

            <SelectFiltro
              label="Método HTTP"
              value={filtroMetodo}
              onChange={setFiltroMetodo}
              options={METODOS_HTTP}
              placeholder="Todos"
            />

            <SelectFiltro
              label="Status HTTP"
              value={filtroStatus}
              onChange={setFiltroStatus}
              options={statusCodes.map(String)}
              placeholder="Todos"
            />

            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium text-gray-600">Endpoint</label>
              <input
                type="text"
                placeholder="Buscar endpoint..."
                value={filtroEndpoint}
                onChange={e => setFiltroEndpoint(e.target.value)}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm w-48
                           focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer pb-2">
              <input
                type="checkbox"
                checked={filtroSoloErrores}
                onChange={e => setFiltroSoloErrores(e.target.checked)}
                className="w-4 h-4 accent-blue-700"
              />
              Solo errores
            </label>
          </div>
        </div>
      )}

      {/* ── Mensajes de estado ── */}
      {error && (
        <p className="text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 text-sm mb-4">
          {error}
        </p>
      )}

      {loading && (
        <p className="text-gray-500 animate-pulse text-sm">Cargando logs...</p>
      )}

      {/* ── Resultados ── */}
      {buscado && !loading && (
        <>
          <p className="text-xs text-gray-500 mb-3">
            {logsFiltrados.length === 0
              ? hayFiltrosActivos
                ? `No hay logs que coincidan con los filtros aplicados. (${logs.length} total en el rango)`
                : 'No se encontraron logs en el rango seleccionado.'
              : <>
                  Mostrando <span className="font-medium text-gray-700">{logsFiltrados.length}</span>
                  {hayFiltrosActivos && <> de <span className="font-medium text-gray-700">{logs.length}</span></>}
                  {' '}log(s) — del{' '}
                  {new Date(fechaInicio + 'T00:00:00').toLocaleDateString('es-CL')} al{' '}
                  {new Date(fechaFin    + 'T00:00:00').toLocaleDateString('es-CL')}
                </>
            }
          </p>

          {logsFiltrados.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full text-sm border border-gray-200 rounded-lg overflow-hidden">
                <thead className="bg-blue-700 text-white">
                  <tr>
                    <th className="px-4 py-3 text-left">ID</th>
                    <th className="px-4 py-3 text-left">Microservicio</th>
                    <th className="px-4 py-3 text-left">Endpoint</th>
                    <th className="px-4 py-3 text-left">Método</th>
                    <th className="px-4 py-3 text-left">Status</th>
                    <th className="px-4 py-3 text-left">Tiempo (ms)</th>
                    <th className="px-4 py-3 text-left">Fecha</th>
                    <th className="px-4 py-3 text-left">Error</th>
                  </tr>
                </thead>
                <tbody>
                  {logsFiltrados.map((log, idx) => (
                    <tr key={`${log.microservicio}-${log.id}-${idx}`} className="border-t border-gray-100 hover:bg-gray-50">
                      <td className="px-4 py-2 text-gray-500">#{log.id}</td>
                      <td className="px-4 py-2">
                        <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                          {log.microservicio}
                        </span>
                      </td>
                      <td className="px-4 py-2 font-mono text-xs text-gray-700">{log.endpoint}</td>
                      <td className="px-4 py-2">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          log.metodoHttp === 'GET'    ? 'bg-green-100 text-green-700'   :
                          log.metodoHttp === 'POST'   ? 'bg-yellow-100 text-yellow-700' :
                          log.metodoHttp === 'PUT'    ? 'bg-orange-100 text-orange-700' :
                          log.metodoHttp === 'DELETE' ? 'bg-red-100 text-red-700'       :
                                                        'bg-gray-100 text-gray-700'
                        }`}>
                          {log.metodoHttp}
                        </span>
                      </td>
                      <td className="px-4 py-2">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          log.status >= 500 ? 'bg-red-100 text-red-700'       :
                          log.status >= 400 ? 'bg-orange-100 text-orange-700' :
                          log.status >= 200 ? 'bg-green-100 text-green-700'   :
                                              'bg-gray-100 text-gray-700'
                        }`}>
                          {log.status}
                        </span>
                      </td>
                      <td className="px-4 py-2 text-gray-600">{log.tiempoRespuesta ?? '—'}</td>
                      <td className="px-4 py-2 text-gray-500 whitespace-nowrap">{formatFecha(log.fecha)}</td>
                      <td className="px-4 py-2 text-red-500 text-xs max-w-xs truncate" title={log.errorMensaje}>
                        {log.errorMensaje ?? '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default ReporteTab;
