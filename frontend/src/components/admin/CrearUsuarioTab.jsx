import { useState } from 'react';
import api from '../../api/axios';
import { REGIONES, COMUNAS_POR_REGION } from '../../data/chileData';

const ESPECIALIDADES = [
  'Cardiología', 'Pediatría', 'Neurología',
  'Traumatología', 'Medicina General', 'Dermatología', 'Ginecología',
];

const ROLES = ['PACIENTE', 'MEDICO', 'ADMIN'];

const FORM_INICIAL = {
  rut:         '',
  dv:          '',
  name:        '',
  lastname:    '',
  email:       '',
  phone:       '',
  region:      '',
  comuna:      '',
  address:     '',
  role:        'PACIENTE',
  especialidad:'',
};

const CrearUsuarioTab = () => {
  const [form, setForm]       = useState(FORM_INICIAL);
  const [errors, setErrors]   = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  const comunas = COMUNAS_POR_REGION[form.region] ?? [];

  // ─── Handlers ─────────────────────────────────────────────────
  const handleChange = (e) => {
    const { name, value } = e.target;
    setErrors(prev => ({ ...prev, [name]: '' })); // limpiar error del campo

    if (name === 'region') {
      setForm(prev => ({ ...prev, region: value, comuna: '' }));
    } else if (name === 'role') {
      setForm(prev => ({ ...prev, role: value, especialidad: '' }));
    } else {
      setForm(prev => ({ ...prev, [name]: value }));
    }
  };

  // ─── Validación ───────────────────────────────────────────────
  const validate = () => {
    const e = {};
    if (!form.rut.trim())      e.rut      = 'El RUT es obligatorio.';
    if (!form.dv.trim())       e.dv       = 'El dígito verificador es obligatorio.';
    if (!form.name.trim())     e.name     = 'El nombre es obligatorio.';
    if (!form.lastname.trim()) e.lastname = 'El apellido es obligatorio.';
    if (!form.email.trim())    e.email    = 'El email es obligatorio.';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'El email no tiene un formato válido.';
    if (!form.phone.trim())    e.phone    = 'El teléfono es obligatorio.';
    if (!form.region)          e.region   = 'La región es obligatoria.';
    if (!form.comuna)          e.comuna   = 'La comuna es obligatoria.';
    if (!form.address.trim())  e.address  = 'La dirección es obligatoria.';
    return e;
  };

  // ─── Envío ────────────────────────────────────────────────────
  const handleSubmit = async () => {
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    // Contraseña temporal: rut + "123"  (sin puntos ni guiones)
    const autoPassword = form.rut.replace(/\D/g, '') + '123';

    try {
      await api.post('/users', {
        ...form,
        password:     autoPassword,
        especialidad: form.role === 'MEDICO' ? (form.especialidad || null) : null,
      });

      setSuccess(
        `Usuario creado exitosamente.\n` +
        `Contraseña temporal asignada: ${autoPassword}\n` +
        `El usuario deberá cambiarla al iniciar sesión por primera vez.`
      );
      setForm(FORM_INICIAL);
      setErrors({});
    } catch (err) {
      if (err.response?.status === 409) {
        setError('Ya existe un usuario con ese RUT o correo electrónico.');
      } else if (err.response?.status === 400) {
        setError('Datos inválidos. Revisa que todos los campos estén correctos.');
      } else {
        setError('Error al crear el usuario. Verifica que el servidor esté activo.');
      }
    } finally {
      setLoading(false);
    }
  };

  // ─── Clases reutilizables ─────────────────────────────────────
  const inputCls = (field) =>
    `w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
      errors[field] ? 'border-red-400 bg-red-50' : 'border-gray-300'
    }`;

  const errMsg = (field) =>
    errors[field] ? (
      <p className="text-red-500 text-xs mt-1">{errors[field]}</p>
    ) : null;

  return (
    <div className="max-w-2xl">
      <h2 className="text-base font-semibold text-gray-700 mb-5">Nuevo usuario</h2>

      {/* Alerta de éxito */}
      {success && (
        <div className="mb-5 bg-green-50 border border-green-200 text-green-700 rounded-lg p-4 text-sm whitespace-pre-line">
          {success}
        </div>
      )}

      {/* Alerta de error */}
      {error && (
        <div className="mb-5 bg-red-50 border border-red-200 text-red-600 rounded-lg p-4 text-sm">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">

        {/* RUT */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            RUT <span className="text-red-400">*</span>
          </label>
          <input
            name="rut"
            value={form.rut}
            onChange={handleChange}
            placeholder="12345678"
            className={inputCls('rut')}
          />
          {errMsg('rut')}
        </div>

        {/* DV */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Dígito Verificador <span className="text-red-400">*</span>
          </label>
          <input
            name="dv"
            value={form.dv}
            onChange={handleChange}
            placeholder="K"
            maxLength={1}
            className={inputCls('dv')}
          />
          {errMsg('dv')}
        </div>

        {/* Nombre */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Nombre <span className="text-red-400">*</span>
          </label>
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            className={inputCls('name')}
          />
          {errMsg('name')}
        </div>

        {/* Apellido */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Apellido <span className="text-red-400">*</span>
          </label>
          <input
            name="lastname"
            value={form.lastname}
            onChange={handleChange}
            className={inputCls('lastname')}
          />
          {errMsg('lastname')}
        </div>

        {/* Email */}
        <div className="sm:col-span-2">
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Email <span className="text-red-400">*</span>
          </label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            className={inputCls('email')}
          />
          {errMsg('email')}
        </div>

        {/* Teléfono */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Teléfono <span className="text-red-400">*</span>
          </label>
          <input
            name="phone"
            value={form.phone}
            onChange={handleChange}
            placeholder="+56912345678"
            className={inputCls('phone')}
          />
          {errMsg('phone')}
        </div>

        {/* Región */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Región <span className="text-red-400">*</span>
          </label>
          <select
            name="region"
            value={form.region}
            onChange={handleChange}
            className={inputCls('region')}
          >
            <option value="">Selecciona una región</option>
            {REGIONES.map(r => (
              <option key={r.id} value={r.nombre}>{r.nombre}</option>
            ))}
          </select>
          {errMsg('region')}
        </div>

        {/* Comuna */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Comuna <span className="text-red-400">*</span>
          </label>
          <select
            name="comuna"
            value={form.comuna}
            onChange={handleChange}
            disabled={!form.region}
            className={inputCls('comuna')}
          >
            <option value="">
              {form.region ? 'Selecciona una comuna' : 'Primero selecciona una región'}
            </option>
            {comunas.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
          {errMsg('comuna')}
        </div>

        {/* Dirección */}
        <div className="sm:col-span-2">
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Dirección <span className="text-red-400">*</span>
          </label>
          <input
            name="address"
            value={form.address}
            onChange={handleChange}
            placeholder="Calle, número, depto..."
            className={inputCls('address')}
          />
          {errMsg('address')}
        </div>

        {/* Rol */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Rol <span className="text-red-400">*</span>
          </label>
          <select
            name="role"
            value={form.role}
            onChange={handleChange}
            className={inputCls('role')}
          >
            {ROLES.map(r => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>
        </div>

        {/* Especialidad (solo si el rol es MEDICO) */}
        {form.role === 'MEDICO' && (
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Especialidad
            </label>
            <select
              name="especialidad"
              value={form.especialidad}
              onChange={handleChange}
              className={inputCls('especialidad')}
            >
              <option value="">Sin especialidad (asignar luego)</option>
              {ESPECIALIDADES.map(esp => (
                <option key={esp} value={esp}>{esp}</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Preview de contraseña temporal */}
      {form.rut.trim() && (
        <div className="mt-5 bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-sm text-yellow-800">
          <strong>Contraseña temporal que se asignará:</strong>{' '}
          <code className="bg-yellow-100 px-1 rounded">
            {form.rut.replace(/\D/g, '')}123
          </code>
          <p className="text-xs mt-1 text-yellow-600">
            El usuario podrá cambiarla desde su perfil después del primer inicio de sesión.
          </p>
        </div>
      )}

      {/* Botón */}
      <button
        onClick={handleSubmit}
        disabled={loading}
        className="mt-6 bg-blue-700 hover:bg-blue-800 text-white px-6 py-2 rounded-lg
                   text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {loading ? 'Creando usuario...' : 'Crear Usuario'}
      </button>
    </div>
  );
};

export default CrearUsuarioTab;