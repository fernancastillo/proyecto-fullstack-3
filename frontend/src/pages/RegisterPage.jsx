import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const ROLE_BY_DOMAIN = {
  'medico.com': { value: 'MEDICO', label: 'Médico', color: 'bg-sky-100 text-sky-700 border-sky-200' },
  'admin.com':  { value: 'ADMIN',  label: 'Administrador', color: 'bg-violet-100 text-violet-700 border-violet-200' },
};

const DEFAULT_ROLE = { value: 'USUARIO', label: 'Usuario', color: 'bg-slate-100 text-slate-600 border-slate-200' };

const getRoleFromEmail = (email) => {
  const domain = email.split('@')[1]?.toLowerCase();
  return ROLE_BY_DOMAIN[domain] ?? DEFAULT_ROLE;
};

const RegisterPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const detectedRole = formData.email.includes('@') ? getRoleFromEmail(formData.email) : null;

  const validate = () => {
    const newErrors = {};
    if (!formData.username.trim()) {
      newErrors.username = 'El nombre de usuario es obligatorio.';
    } else if (formData.username.trim().length < 3) {
      newErrors.username = 'Debe tener al menos 3 caracteres.';
    }
    if (!formData.email.trim()) {
      newErrors.email = 'El correo es obligatorio.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Ingresa un correo válido.';
    }
    if (!formData.password) {
      newErrors.password = 'La contraseña es obligatoria.';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Debe tener al menos 6 caracteres.';
    }
    return newErrors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);

    // ---------------------------------------------------------
    // TODO: Reemplazar este bloque con la llamada real al backend.
    // El rol NO se envía desde el frontend; el backend lo asigna
    // según el dominio del correo (o desde la BD).
    // Ejemplo:
    //   await axios.post('/bff/users', {
    //     username: formData.username,
    //     email: formData.email,
    //     password: formData.password,
    //   });
    // ---------------------------------------------------------
    await new Promise((res) => setTimeout(res, 900));
    // ---------------------------------------------------------

    setLoading(false);
    setSuccess(true);
    setTimeout(() => navigate('/login'), 1800);
  };

  const inputBase =
    'w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm placeholder-slate-300 bg-slate-50 transition-colors duration-150 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100';

  const ErrorMsg = ({ msg }) =>
    msg ? (
      <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1">
        <svg className="w-3.5 h-3.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
        </svg>
        {msg}
      </p>
    ) : null;

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">

        {/* Header */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 mb-4">
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h1 className="text-2xl font-semibold text-slate-800 tracking-tight">Crear cuenta</h1>
          <p className="text-sm text-slate-500 mt-1">Completa los datos para registrarte</p>
        </div>

        {/* Success banner */}
        {success && (
          <div className="mb-4 flex items-center gap-3 bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl">
            <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Cuenta creada correctamente. Redirigiendo al login...
          </div>
        )}

        {/* Card */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
          <form onSubmit={handleSubmit} noValidate className="space-y-5">

            {/* Username */}
            <div>
              <label htmlFor="username" className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                Nombre de usuario
              </label>
              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="ej: dr_garcia"
                className={`${inputBase} ${errors.username ? 'border-red-400 bg-red-50 focus:border-red-400 focus:ring-red-100' : 'border-slate-200'}`}
              />
              <ErrorMsg msg={errors.username} />
            </div>

            {/* Email + rol detectado */}
            <div>
              <label htmlFor="email" className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                Correo electrónico
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="correo@ejemplo.com"
                className={`${inputBase} ${errors.email ? 'border-red-400 bg-red-50 focus:border-red-400 focus:ring-red-100' : 'border-slate-200'}`}
              />
              <ErrorMsg msg={errors.email} />

              {/* Badge de rol detectado */}
              {detectedRole && !errors.email && (
                <div className="mt-2 flex items-center gap-2">
                  <span className="text-xs text-slate-400">Rol asignado:</span>
                  <span className={`inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full border ${detectedRole.color}`}>
                    <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                    {detectedRole.label}
                  </span>
                </div>
              )}
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
                Contraseña
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Mínimo 6 caracteres"
                  className={`${inputBase} pr-10 ${errors.password ? 'border-red-400 bg-red-50 focus:border-red-400 focus:ring-red-100' : 'border-slate-200'}`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? (
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
              </div>
              <ErrorMsg msg={errors.password} />
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading || success}
              className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium rounded-lg transition-colors duration-150 flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                <>
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  Registrando...
                </>
              ) : (
                'Crear cuenta'
              )}
            </button>
          </form>
        </div>

        {/* Footer link */}
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

export default RegisterPage;
