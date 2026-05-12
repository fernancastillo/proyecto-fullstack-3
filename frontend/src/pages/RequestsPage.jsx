import { useState } from 'react';

const RequestsPage = () => {
  const [especialidad, setEspecialidad] = useState('');
  const [descripcion, setDescripcion] = useState('');
  // Estado para guardar la lista de solicitudes
  const [historial, setHistorial] = useState([]);

  const especialidades = ["Cardiología", "Pediatría", "Neurología", "Dermatología", "Ginecología", "Medicina General"];

  const handleConfirmar = () => {
    if (!especialidad || !descripcion) {
      alert('Por favor, rellene todos los campos');
      return;
    }

    // Crear el nuevo objeto de solicitud con fecha y hora actual
    const nuevaSolicitud = {
      fechaHora: new Date().toLocaleString(),
      especialidad,
      descripcion
    };

    // Guardar en el historial y limpiar campos
    setHistorial([nuevaSolicitud, ...historial]);
    setEspecialidad('');
    setDescripcion('');
  };

  return (
    <div className="flex flex-col items-center min-h-screen p-4 bg-gray-50">
      <h1 className="text-2xl font-bold text-blue-700 mb-6 text-center">Solicitudes de Atención</h1>

      {/* Formulario */}
      <div className="bg-blue-700 text-white p-8 rounded-lg shadow-lg w-full max-w-md flex flex-col items-center mb-10">
        <h1 className="text-2xl font-bold mb-2">Solicitud</h1>
        <div className="w-full flex flex-col gap-4">
          <div className="flex flex-col items-center">
            <h2 className="mb-2 font-semibold">Especialidad</h2>
            <select 
              value={especialidad} 
              onChange={(e) => setEspecialidad(e.target.value)}
              className="text-black p-2 rounded w-full"
            >
              <option value="" disabled>Seleccione...</option>
              {especialidades.map((esp, i) => <option key={i} value={esp}>{esp}</option>)}
            </select>
          </div>

          <div className="flex flex-col items-center">
            <h2 className="mb-2 font-semibold">Descripción</h2>
            <textarea 
              value={descripcion} 
              onChange={(e) => setDescripcion(e.target.value)}
              className="text-black p-2 rounded w-full min-h-[80px] resize-none"
            />
          </div>

          <button 
            onClick={handleConfirmar}
            className="mt-4 bg-white text-blue-700 font-bold py-2 px-4 rounded-md hover:bg-blue-50 active:scale-95 transition-all"
          >
            Confirmar Solicitud
          </button>
        </div>
      </div>

      {/* Tabla de Resultados */}
      <div className="w-full max-w-4xl overflow-x-auto">
        <table className="min-w-full bg-white border border-gray-200 shadow-md rounded-lg">
          <thead className="bg-blue-700 text-white">
            <tr>
              <th className="py-3 px-4 text-left">Fecha y Hora</th>
              <th className="py-3 px-4 text-left">Especialización</th>
              <th className="py-3 px-4 text-left">Descripción</th>
            </tr>
          </thead>
          <tbody className="text-gray-700">
            {historial.length === 0 ? (
              <tr>
                <td colSpan="3" className="py-4 text-center text-gray-400 italic">No hay solicitudes registradas</td>
              </tr>
            ) : (
              historial.map((item, index) => (
                <tr key={index} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 text-sm">{item.fechaHora}</td>
                  <td className="py-3 px-4 font-medium">{item.especialidad}</td>
                  <td className="py-3 px-4 text-sm">{item.descripcion}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RequestsPage;
