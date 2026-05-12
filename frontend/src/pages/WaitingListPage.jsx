import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const MOCK_DATA = [
  {
    id: 1,
    patientName: 'María González',
    doctorName: 'Dra. Ana Muñoz',
    doctorUsername: 'usuario_demo',
    specialty: 'Traumatología',
    datetime: '2025-05-12T08:30',
    priority: 'URGENTE',
    status: 'EN_ATENCION',
  },
  {
    id: 2,
    patientName: 'Jorge Castillo',
    doctorName: 'Dr. Carlos Ramírez',
    doctorUsername: 'dr_ramirez',
    specialty: 'Cardiología',
    datetime: '2025-05-12T08:45',
    priority: 'URGENTE',
    status: 'ESPERANDO',
  },
  {
    id: 3,
    patientName: 'Camila Rojas',
    doctorName: 'Dra. Ana Muñoz',
    doctorUsername: 'usuario_demo',
    specialty: 'Traumatología',
    datetime: '2025-05-12T09:00',
    priority: 'URGENTE',
    status: 'ESPERANDO',
  },
  {
    id: 4,
    patientName: 'Luis Herrera',
    doctorName: 'Dr. Felipe Mora',
    doctorUsername: 'dr_mora',
    specialty: 'Neurología',
    datetime: '2025-05-12T09:10',
    priority: 'URGENTE',
    status: 'ESPERANDO',
  },
  {
    id: 5,
    patientName: 'Valentina Soto',
    doctorName: 'Dra. Ana Muñoz',
    doctorUsername: 'usuario_demo',
    specialty: 'Traumatología',
    datetime: '2025-05-12T09:30',
    priority: 'NORMAL',
    status: 'ESPERANDO',
  },
  {
    id: 6,
    patientName: 'Roberto Díaz',
    doctorName: 'Dr. Carlos Ramírez',
    doctorUsername: 'dr_ramirez',
    specialty: 'Cardiología',
    datetime: '2025-05-12T09:45',
    priority: 'NORMAL',
    status: 'ESPERANDO',
  },
  {
    id: 7,
    patientName: 'Andrea Fuentes',
    doctorName: 'Dr. Felipe Mora',
    doctorUsername: 'dr_mora',
    specialty: 'Neurología',
    datetime: '2025-05-12T10:00',
    priority: 'NORMAL',
    status: 'ESPERANDO',
  },
  {
    id: 8,
    patientName: 'Tomás Vargas',
    doctorName: 'Dra. Ana Muñoz',
    doctorUsername: 'usuario_demo',
    specialty: 'Traumatología',
    datetime: '2025-05-12T10:15',
    priority: 'NORMAL',
    status: 'ESPERANDO',
  },
  {
    id: 9,
    patientName: 'Isidora Parra',
    doctorName: 'Dr. Carlos Ramírez',
    doctorUsername: 'dr_ramirez',
    specialty: 'Cardiología',
    datetime: '2025-05-12T10:30',
    priority: 'NORMAL',
    status: 'ESPERANDO',
  },
];

const PRIORITY_ORDER = { URGENTE: 0, NORMAL: 1 };

const sortList = (list) =>
  [...list].sort((a, b) => {
    const pDiff = PRIORITY_ORDER[a.priority] - PRIORITY_ORDER[b.priority];
    if (pDiff !== 0) return pDiff;
    return new Date(a.datetime) - new Date(b.datetime);
  });

const formatDatetime = (iso) => {
  const date = new Date(iso);
  return {
    date: date.toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' }),
    time: date.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' }),
  };
};

const PriorityBadge = ({ priority }) => {
  const styles = {
    URGENTE: 'bg-red-100 text-red-700 border-red-200',
    NORMAL: 'bg-slate-100 text-slate-600 border-slate-200',
  };
  return (
    <span className={`inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full border ${styles[priority]}`}>
      {priority === 'URGENTE' && (
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
    ESPERANDO: 'bg-amber-100 text-amber-700 border-amber-200',
    EN_ATENCION: 'bg-blue-100 text-blue-700 border-blue-200',
  };
  const labels = {
    ESPERANDO: 'Esperando',
    EN_ATENCION: 'En atención',
  };
  const dots = {
    ESPERANDO: 'bg-amber-500',
    EN_ATENCION: 'bg-blue-500',
  };
  return (
    <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full border ${styles[status]}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dots[status]}`} />
      {labels[status]}
    </span>
  );
};

const WaitingListPage = () => {
  const { user } = useAuth();
  const [list, setList] = useState(() => sortList(MOCK_DATA));
  const [confirming, setConfirming] = useState(null); 
  const [dismissing, setDismissing] = useState(null); 

  const isDoctor = user?.role === 'MEDICO';
  const isAdmin = user?.role === 'ADMIN';
  const canMarkAttended = (entry) =>
    (isDoctor && entry.doctorUsername === user?.username) || isAdmin;

  const handleMarkAttended = (id) => {
    setConfirming(null);
    setDismissing(id);

    setTimeout(() => {
      setList((prev) => sortList(prev.filter((e) => e.id !== id)));
      setDismissing(null);
    }, 400);
  };

  return (
    <div className="max-w-6xl mx-auto">

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800 tracking-tight">Lista de espera</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {list.length} {list.length === 1 ? 'paciente en espera' : 'pacientes en espera'} · ordenado por prioridad
          </p>
        </div>
        <div className="hidden sm:flex items-center gap-4 text-xs text-slate-500">
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-red-400" /> Urgente
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-slate-300" /> Normal
          </span>
        </div>
      </div>

      {list.length === 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm flex flex-col items-center justify-center py-20 text-center">
          <div className="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center mb-4">
            <svg className="w-7 h-7 text-green-600" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p className="text-slate-700 font-medium">¡Sin pacientes en espera!</p>
          <p className="text-slate-400 text-sm mt-1">Todos los pacientes han sido atendidos.</p>
        </div>
      )}

      {list.length > 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-5 py-3 w-10">#</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Prioridad</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Paciente</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Médico</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Especialidad</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Fecha y hora</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Estado</th>
                {(isDoctor || isAdmin) && (
                  <th className="px-4 py-3 w-10" />
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {list.map((entry, index) => {
                const { date, time } = formatDatetime(entry.datetime);
                const isDismissing = dismissing === entry.id;
                const isConfirming = confirming === entry.id;
                const canAct = canMarkAttended(entry);

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
                          <span className="text-xs font-semibold text-blue-700">
                            {entry.patientName.split(' ').map((n) => n[0]).slice(0, 2).join('')}
                          </span>
                        </div>
                        <span className="font-medium text-slate-800">{entry.patientName}</span>
                      </div>
                    </td>

                    <td className="px-4 py-4 text-slate-600">{entry.doctorName}</td>

                    <td className="px-4 py-4 text-slate-500">{entry.specialty}</td>

                    <td className="px-4 py-4">
                      <span className="text-slate-700">{date}</span>
                      <span className="text-slate-400 ml-1.5 tabular-nums">{time}</span>
                    </td>

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
                              onClick={() => handleMarkAttended(entry.id)}
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

      {isDoctor && list.some((e) => e.doctorUsername === user?.username) && (
        <p className="mt-4 text-xs text-slate-400 text-center">
          Solo puedes marcar como atendidos los pacientes asignados a ti.
        </p>
      )}
    </div>
  );
};

export default WaitingListPage;
