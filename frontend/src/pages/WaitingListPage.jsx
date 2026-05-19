import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const PRIORITY_ORDER = { ALTA: 0, URGENTE: 0, MEDIA: 1, NORMAL: 1, BAJA: 2 };

const sortList = (list) =>
  [...list].sort((a, b) => {
    const pDiff = (PRIORITY_ORDER[a.priority] ?? 99) - (PRIORITY_ORDER[b.priority] ?? 99);
    if (pDiff !== 0) return pDiff;
    return new Date(a.requestDate) - new Date(b.requestDate);
  });

const PriorityBadge = ({ priority }) => {
  const styles = {
    ALTA:    'bg-red-100 text-red-700 border-red-200',
    URGENTE: 'bg-red-100 text-red-700 border-red-200',
    MEDIA:   'bg-amber-100 text-amber-700 border-amber-200',
    NORMAL:  'bg-slate-100 text-slate-600 border-slate-200',
    BAJA:    'bg-slate-100 text-slate-600 border-slate-200',
  };
  const isHigh = priority === 'ALTA' || priority === 'URGENTE';
  return (
    <span className={`inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full border ${styles[priority] ?? 'bg-gray-100 text-gray-600 border-gray-200'}`}>
      {isHigh && (
        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
        </svg>
      )}
      {priority}
    </span>
  );
};

const StatusBadge = ({ status }) => {
  const styles = {
    EN_ESPERA:   'bg-amber-100 text-amber-700 border-amber-200',
    ESPERANDO:   'bg-amber-100 text-amber-700 border-amber-200',
    EN_ATENCION: 'bg-blue-100  text-blue-700  border-blue-200',
    ATENDIDO:    'bg-green-100 text-green-700 border-green-200',
  };
  const labels = {
    EN_ESPERA:   'En espera',
    ESPERANDO:   'Esperando',
    EN_ATENCION: 'En atención',
    ATENDIDO:    'Atendido',
  };
  const dots = {
    EN_ESPERA:   'bg-amber-500',
    ESPERANDO:   'bg-amber-500',
    EN_ATENCION: 'bg-blue-500',
    ATENDIDO:    'bg-green-500',
  };
  return (
    <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full border ${styles[status] ?? 'bg-gray-100 text-gray-600 border-gray-200'}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dots[status] ?? 'bg-gray-400'}`} />
      {labels[status] ?? status}
    </span>
  );
};

const WaitingListPage = () => {
  const { user } = useAuth();
  const [allList,    setAllList]    = useState([]);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState('');
  const [showAll,    setShowAll]    = useState(false);
  const [confirming, setConfirming] = useState(null);
  const [dismissing, setDismissing] = useState(null);

  const isDoctor = user?.role === 'MEDICO';
  const isAdmin  = user?.role === 'ADMIN';

  useEffect(() => {
    api.get('/waiting-list/enriched')
      .then(res => {
        const active = res.data.filter(e => e.status !== 'ATENDIDO');
        setAllList(sortList(active));
      })
      .catch(() => setError('No se pudo cargar la lista de espera.'))
      .finally(() => setLoading(false));
  }, []);

  // Médico ve solo sus pacientes por defecto; el toggle muestra todos
  const displayList = isDoctor && !showAll
    ? allList.filter(e => e.medicoId === user?.id)
    : allList;

  const canMarkAttended = (entry) =>
    (isDoctor && entry.medicoId === user?.id) || isAdmin;

  const handleMarkAttended = async (entry) => {
    setConfirming(null);
    setDismissing(entry.id);
    try {
      await api.put(`/waiting-list/${entry.id}`, {
        id:          entry.id,
        userId:      entry.userId,
        medicoId:    entry.medicoId,
        specialty:   entry.specialty,
        priority:    entry.priority,
        status:      'ATENDIDO',
        requestDate: entry.requestDate,
      });
      setTimeout(() => {
        setAllList(prev => sortList(prev.filter(e => e.id !== entry.id)));
        setDismissing(null);
      }, 400);
    } catch {
      setDismissing(null);
      alert('Error al actualizar el estado. Intenta de nuevo.');
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center py-20">
      <div className="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full" />
    </div>
  );

  if (error) return (
    <p className="text-red-500 bg-red-50 p-4 rounded-lg">{error}</p>
  );

  return (
    <div className="max-w-6xl mx-auto">

      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800 tracking-tight">Lista de espera</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {displayList.length}{' '}
            {displayList.length === 1 ? 'paciente en espera' : 'pacientes en espera'}
            {isDoctor && !showAll ? ' asignados a ti' : ' · total'}{' '}
            · ordenado por prioridad
          </p>
        </div>

        <div className="flex items-center gap-5">
          {/* Toggle solo visible para médicos */}
          {isDoctor && (
            <label className="flex items-center gap-2 cursor-pointer select-none">
              <button
                onClick={() => setShowAll(v => !v)}
                className={`relative w-10 h-5 rounded-full transition-colors focus:outline-none ${showAll ? 'bg-blue-600' : 'bg-slate-300'}`}
              >
                <span className={`absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200 ${showAll ? 'translate-x-5' : ''}`} />
              </button>
              <span className="text-sm text-slate-600">Ver todos los pacientes</span>
            </label>
          )}

          <div className="hidden sm:flex items-center gap-4 text-xs text-slate-500">
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-red-400" /> Alta
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-amber-400" /> Media
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-slate-300" /> Normal/Baja
            </span>
          </div>
        </div>
      </div>

      {/* Empty state */}
      {displayList.length === 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm flex flex-col items-center justify-center py-20 text-center">
          <div className="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center mb-4">
            <svg className="w-7 h-7 text-green-600" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p className="text-slate-700 font-medium">¡Sin pacientes en espera!</p>
          <p className="text-slate-400 text-sm mt-1">
            {isDoctor && !showAll
              ? 'No tienes pacientes asignados en espera.'
              : 'Todos los pacientes han sido atendidos.'}
          </p>
        </div>
      )}

      {/* Table */}
      {displayList.length > 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-5 py-3 w-10">#</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Prioridad</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Paciente</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Médico</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Especialidad</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Fecha solicitud</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Estado</th>
                {(isDoctor || isAdmin) && <th className="px-4 py-3 w-10" />}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {displayList.map((entry, index) => {
                const isDismissing = dismissing === entry.id;
                const isConfirming = confirming === entry.id;
                const canAct       = canMarkAttended(entry);
                const dateStr      = entry.requestDate
                  ? new Date(entry.requestDate).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' })
                  : '—';
                const initials = entry.patientName
                  ?.split(' ').map(n => n[0]).slice(0, 2).join('') ?? '?';

                return (
                  <tr
                    key={entry.id}
                    className={`transition-all duration-300 ${
                      isDismissing ? 'opacity-0 scale-95' : 'opacity-100'
                    } ${canAct ? 'hover:bg-slate-50/70' : 'hover:bg-slate-50/40'}`}
                  >
                    <td className="px-5 py-4">
                      <span className="text-xs font-semibold text-slate-400 tabular-nums">{index + 1}</span>
                    </td>

                    <td className="px-4 py-4">
                      <PriorityBadge priority={entry.priority} />
                    </td>

                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                          <span className="text-xs font-semibold text-blue-700">{initials}</span>
                        </div>
                        <span className="font-medium text-slate-800">{entry.patientName ?? '—'}</span>
                      </div>
                    </td>

                    <td className="px-4 py-4 text-slate-600">{entry.doctorName ?? '—'}</td>

                    <td className="px-4 py-4 text-slate-500">
                      {entry.doctorEspecialidad || entry.specialty || '—'}
                    </td>

                    <td className="px-4 py-4 text-slate-600 tabular-nums">{dateStr}</td>

                    <td className="px-4 py-4">
                      <StatusBadge status={entry.status} />
                    </td>

                    {(isDoctor || isAdmin) && (
                      <td className="px-4 py-4 text-right">
                        {canAct && !isConfirming && (
                          <button
                            onClick={() => setConfirming(entry.id)}
                            className="text-xs font-medium text-blue-600 hover:text-blue-800 hover:bg-blue-50 px-3 py-1.5 rounded-lg transition-colors whitespace-nowrap"
                          >
                            Marcar atendido
                          </button>
                        )}
                        {canAct && isConfirming && (
                          <div className="flex items-center justify-end gap-2">
                            <span className="text-xs text-slate-500 hidden sm:inline">¿Confirmar?</span>
                            <button
                              onClick={() => handleMarkAttended(entry)}
                              className="text-xs font-medium text-white bg-green-600 hover:bg-green-700 px-3 py-1.5 rounded-lg transition-colors"
                            >
                              Sí
                            </button>
                            <button
                              onClick={() => setConfirming(null)}
                              className="text-xs font-medium text-slate-500 hover:text-slate-700 hover:bg-slate-100 px-3 py-1.5 rounded-lg transition-colors"
                            >
                              No
                            </button>
                          </div>
                        )}
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {isDoctor && !showAll && allList.some(e => e.medicoId !== user?.id) && (
        <p className="mt-4 text-xs text-slate-400 text-center">
          Hay más pacientes en la lista de espera general. Activa &quot;Ver todos los pacientes&quot; para verlos.
        </p>
      )}
    </div>
  );
};

export default WaitingListPage;