import { useState, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import { REGIONES, COMUNAS_POR_REGION } from '../data/chileData';
import { validateRutChileno, cleanRut } from '../utils/rutValidator';

// ─── Helpers ──────────────────────────────────────────────────────

const EMPTY_FORM = {
  rut:       '',   // input unificado "12.345.678-9"
  name:      '',
  lastname:  '',
  email:     '',
  password:  '',
  phone:     '',
  region:    '',
  comuna:    '',
  address:   '',
};

const inputBase =
  'w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm placeholder-slate-300 bg-slate-50 ' +
  'transition-colors duration-150 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100';

const errorInput = 'border-red-400 bg-red-50 focus:border-red-400 focus:ring-red-100';
const normalInput = 'border-slate-200';

// ─── Componentes auxiliares ────────────────────────────────────────

const Label = ({ htmlFor, children }) => (
  <label htmlFor={htmlFor} className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
    {children}
  </label>
);

const FieldError = ({ msg }) =>
  msg ? (
    <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1">
      <svg className="w-3.5 h-3.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
      </svg>
      {msg}
    </p>
  ) : null;

const Spinner = ({ label }) => (
  <>
    <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
    </svg>
    {label}
  </>
);

const TogglePasswordBtn = ({ show, onClick }) => (
  <button
    type="button"
    onClick={onClick}
    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
    aria-label={show ? 'Ocultar contraseña' : 'Mostrar contraseña'}
  >
    {show ? (
      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
      </svg>
    ) : (
      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
      </svg>
    )}
  </button>
);

// ─── Validación ────────────────────────────────────────────────────

const validate = (formData) => {
  const errors = {};

  // RUT
  if (!formData.rut.trim()) {
    errors.rut = 'El RUT es obligatorio.';
  } else {
    const { rut, dv } = cleanRut(formData.rut);
    if (!rut || !dv) {
      errors.rut = 'Formato de RUT inválido. Ej: 12.345.678-9';
    } else if (!validateRutChileno(rut, dv)) {
      errors.rut = 'El RUT no es válido.';
    }
  }

  if (!formData.name.trim())
    errors.name = 'El nombre es obligatorio.';

  if (!formData.lastname.trim())
    errors.lastname = 'El apellido es obligatorio.';

  if (!formData.email.trim()) {
    errors.email = 'El correo es obligatorio.';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = 'Ingresa un correo válido.';
  }

  if (!formData.password) {
    errors.password = 'La contraseña es obligatoria.';
  } else if (formData.password.length < 6) {
    errors.password = 'Debe tener al menos 6 caracteres.';
  }

  if (!formData.phone.trim()) {
    errors.phone = 'El teléfono es obligatorio.';
  } else if (!/^\+?[\d\s\-]{9,12}$/.test(formData.phone.trim())) {
    errors.phone = 'Ingresa un teléfono válido (ej: +56912345678).';
  }

  if (!formData.region)
    errors.region = 'Selecciona una región.';

  if (!formData.comuna)
    errors.comuna = 'Selecciona una comuna.';

  if (!formData.address.trim())
    errors.address = 'La dirección es obligatoria.';

  return errors;
};

// ─── Componente principal ──────────────────────────────────────────

const RegisterPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData]         = useState(EMPTY_FORM);
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors]             = useState({});
  const [serverError, setServerError]   = useState('');
  const [loading, setLoading]           = useState(false);
  const [success, setSuccess]           = useState(false);

  // Comunas disponibles según la región seleccionada
  const comunas = useMemo(
    () => COMUNAS_POR_REGION[formData.region] ?? [],
    [formData.region],
  );

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const next = { ...prev, [name]: value };
      // Si cambia la región, resetear la comuna
      if (name === 'region') next.comuna = '';
      return next;
    });
    if (errors[name])  setErrors((prev) => ({ ...prev, [name]: '' }));
    if (serverError)   setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setServerError('');

    // Separar RUT en número y DV
    const { rut, dv } = cleanRut(formData.rut);

    try {
      await api.post('/auth/register', {
        rut,
        dv,
        name:     formData.name.trim(),
        lastname: formData.lastname.trim(),
        email:    formData.email.trim(),
        password: formData.password,
        phone:    formData.phone.trim(),
        region:   formData.region,
        comuna:   formData.comuna,
        address:  formData.address.trim(),
      });

      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Error al crear la cuenta. Intenta nuevamente.';
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-2xl">

        {/* Header */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 mb-4">
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h1 className="text-2xl font-semibold text-slate-800 tracking-tight">Crear cuenta</h1>
          <p className="text-sm text-slate-500 mt-1">Completa tus datos para registrarte como paciente</p>
        </div>

        {/* Banner de éxito */}
        {success && (
          <div className="mb-4 flex items-center gap-3 bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl">
            <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Cuenta creada correctamente. Redirigiendo al login…
          </div>
        )}

        {/* Error del servidor */}
        {serverError && (
          <div className="mb-4 flex items-center gap-2 bg-red-50 border border-red-200 text-red-600 text-sm px-4 py-3 rounded-xl">
            <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            {serverError}
          </div>
        )}

        {/* Card */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
          <form onSubmit={handleSubmit} noValidate className="space-y-6">

            {/* ── Sección: Identificación ── */}
            <SectionTitle>Identificación</SectionTitle>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">

              {/* RUT (número + DV en un solo campo) */}
              <div className="sm:col-span-2">
                <Label htmlFor="rut">RUT</Label>
                <input
                  id="rut"
                  name="rut"
                  type="text"
                  value={formData.rut}
                  onChange={handleChange}
                  placeholder="12.345.678-9"
                  maxLength={12}
                  className={`${inputBase} ${errors.rut ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.rut} />
              </div>

              {/* Nombre */}
              <div>
                <Label htmlFor="name">Nombre</Label>
                <input
                  id="name"
                  name="name"
                  type="text"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Juan"
                  className={`${inputBase} ${errors.name ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.name} />
              </div>

              {/* Apellido */}
              <div>
                <Label htmlFor="lastname">Apellido</Label>
                <input
                  id="lastname"
                  name="lastname"
                  type="text"
                  value={formData.lastname}
                  onChange={handleChange}
                  placeholder="Pérez"
                  className={`${inputBase} ${errors.lastname ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.lastname} />
              </div>
            </div>

            {/* ── Sección: Cuenta ── */}
            <SectionTitle>Cuenta</SectionTitle>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">

              {/* Correo */}
              <div className="sm:col-span-2">
                <Label htmlFor="email">Correo electrónico</Label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="correo@ejemplo.com"
                  className={`${inputBase} ${errors.email ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.email} />
              </div>

              {/* Contraseña */}
              <div className="sm:col-span-2">
                <Label htmlFor="password">Contraseña</Label>
                <div className="relative">
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Mínimo 6 caracteres"
                    className={`${inputBase} pr-10 ${errors.password ? errorInput : normalInput}`}
                  />
                  <TogglePasswordBtn show={showPassword} onClick={() => setShowPassword((p) => !p)} />
                </div>
                <FieldError msg={errors.password} />
              </div>
            </div>

            {/* ── Sección: Contacto y Ubicación ── */}
            <SectionTitle>Contacto y ubicación</SectionTitle>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">

              {/* Teléfono */}
              <div>
                <Label htmlFor="phone">Teléfono</Label>
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="+56912345678"
                  maxLength={12}
                  className={`${inputBase} ${errors.phone ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.phone} />
              </div>

              {/* Región */}
              <div>
                <Label htmlFor="region">Región</Label>
                <select
                  id="region"
                  name="region"
                  value={formData.region}
                  onChange={handleChange}
                  className={`${inputBase} ${errors.region ? errorInput : normalInput}`}
                >
                  <option value="">Selecciona una región</option>
                  {REGIONES.map((r) => (
                    <option key={r.id} value={r.nombre}>{r.nombre}</option>
                  ))}
                </select>
                <FieldError msg={errors.region} />
              </div>

              {/* Comuna */}
              <div>
                <Label htmlFor="comuna">Comuna</Label>
                <select
                  id="comuna"
                  name="comuna"
                  value={formData.comuna}
                  onChange={handleChange}
                  disabled={!formData.region}
                  className={`${inputBase} disabled:opacity-50 disabled:cursor-not-allowed ${errors.comuna ? errorInput : normalInput}`}
                >
                  <option value="">
                    {formData.region ? 'Selecciona una comuna' : 'Primero elige una región'}
                  </option>
                  {comunas.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
                <FieldError msg={errors.comuna} />
              </div>

              {/* Dirección */}
              <div className="sm:col-span-2">
                <Label htmlFor="address">Dirección</Label>
                <input
                  id="address"
                  name="address"
                  type="text"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Av. Ejemplo 123, Depto 4"
                  maxLength={200}
                  className={`${inputBase} ${errors.address ? errorInput : normalInput}`}
                />
                <FieldError msg={errors.address} />
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading || success}
              className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium rounded-lg transition-colors duration-150 flex items-center justify-center gap-2"
            >
              {loading ? <Spinner label="Registrando..." /> : 'Crear cuenta'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-slate-500 mt-6">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-blue-600 font-medium hover:underline">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
};

const SectionTitle = ({ children }) => (
  <div className="flex items-center gap-3">
    <span className="text-xs font-semibold text-slate-400 uppercase tracking-widest">{children}</span>
    <div className="flex-1 h-px bg-slate-100" />
  </div>
);

export default RegisterPage;
