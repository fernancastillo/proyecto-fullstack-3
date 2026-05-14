import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const ProfilePage = () => {
  const { user, login } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({});
  const [formErrors, setFormErrors] = useState({});

  // ─── Cargar perfil ───────────────────────────────────────────
  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await api.get(`/users/${user.id}`);
        setProfile(response.data);
        setFormData(response.data);
      } catch {
        setError('No se pudo cargar tu perfil. Verifica que el servidor esté activo.');
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) fetchProfile();
  }, [user]);

  // ─── Validación ─────────────────────────────────────────────
  const validate = () => {
    const newErrors = {};
    if (!formData.name?.trim()) newErrors.name = 'El nombre es obligatorio.';
    if (!formData.lastname?.trim()) newErrors.lastname = 'El apellido es obligatorio.';
    if (!formData.email?.trim()) {
      newErrors.email = 'El correo es obligatorio.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Ingresa un correo válido.';
    }
    if (!formData.phone?.trim()) newErrors.phone = 'El teléfono es obligatorio.';
    if (!formData.region?.trim()) newErrors.region = 'La región es obligatoria.';
    if (!formData.comuna?.trim()) newErrors.comuna = 'La comuna es obligatoria.';
    if (!formData.address?.trim()) newErrors.address = 'La dirección es obligatoria.';
    if (profile?.role === 'MEDICO' && !formData.especialidad?.trim()) {
      newErrors.especialidad = 'La especialidad es obligatoria para médicos.';
    }
    return newErrors;
  };

  // ─── Handlers ───────────────────────────────────────────────
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const handleEdit = () => {
    setFormData(profile);
    setFormErrors({});
    setIsEditing(true);
  };

  const handleCancel = () => {
    setFormData(profile);
    setFormErrors({});
    setIsEditing(false);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      return;
    }

    setIsSaving(true);
    setError('');
    try {
      const response = await api.put(`/users/${user.id}`, formData);
      setProfile(response.data);
      login({ ...user, ...response.data });
      setIsEditing(false);
      setSuccessMessage('¡Tu perfil ha sido actualizado correctamente!');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch {
      setError('No se pudo actualizar el perfil. Intenta nuevamente.');
    } finally {
      setIsSaving(false);
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

  const inputBase = 'w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm placeholder-slate-300 bg-slate-50 transition-colors duration-150 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100';

  const Field = ({ label, name, placeholder, disabled = false }) => (
    <div>
      <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
        {label}
      </label>
      {isEditing && !disabled ? (
        <>
          <input
            name={name}
            value={formData[name] || ''}
            onChange={handleChange}
            placeholder={placeholder}
            className={`${inputBase} ${formErrors[name] ? 'border-red-400 bg-red-50 focus:border-red-400 focus:ring-red-100' : 'border-slate-200'}`}
          />
          <ErrorMsg msg={formErrors[name]} />
        </>
      ) : (
        <p className="px-4 py-2.5 rounded-lg bg-slate-50 border border-slate-200 text-slate-800 text-sm">
          {profile?.[name] || <span className="text-slate-400 italic">No especificado</span>}
        </p>
      )}
    </div>
  );

  const roleBadge = {
    PACIENTE: 'bg-sky-100 text-sky-700 border-sky-200',
    MEDICO: 'bg-violet-100 text-violet-700 border-violet-200',
    ADMIN: 'bg-amber-100 text-amber-700 border-amber-200',
  };

  // ─── Render ─────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] text-slate-400">
        <svg className="w-6 h-6 animate-spin mr-2" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
        </svg>
        Cargando perfil...
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">

      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Mi Perfil</h1>
          <p className="text-sm text-slate-500 mt-0.5">Consulta y actualiza tus datos personales</p>
        </div>
        {!isEditing && (
          <button
            onClick={handleEdit}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
            Editar perfil
          </button>
        )}
      </div>

      {/* Success banner */}
      {successMessage && (
        <div className="mb-4 flex items-center gap-3 bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl">
          <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {successMessage}
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

      {/* Card */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">

        {/* Avatar header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-8 py-6 flex items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center text-white text-2xl font-bold">
            {profile?.name?.[0]?.toUpperCase() || '?'}
          </div>
          <div>
            <h2 className="text-white text-lg font-semibold">
              {profile?.name} {profile?.lastname}
            </h2>
            <p className="text-blue-100 text-sm">{profile?.email}</p>
            <span className={`inline-flex items-center mt-1.5 text-xs font-medium px-2.5 py-0.5 rounded-full border ${roleBadge[profile?.role] || 'bg-slate-100 text-slate-600 border-slate-200'}`}>
              {profile?.role}
            </span>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSave} noValidate className="px-8 py-6 space-y-5">

          {/* RUT — solo lectura siempre */}
          <div className="flex gap-4">
            <div className="flex-1">
              <Field label="RUT" name="rut" placeholder="12345678" disabled />
            </div>
            <div className="w-24">
              <Field label="DV" name="dv" placeholder="K" disabled />
            </div>
          </div>

          <div className="flex gap-4">
            <div className="flex-1"><Field label="Nombre" name="name" placeholder="Juan" /></div>
            <div className="flex-1"><Field label="Apellido" name="lastname" placeholder="Pérez" /></div>
          </div>

          <Field label="Correo electrónico" name="email" placeholder="correo@ejemplo.com" />
          <Field label="Teléfono" name="phone" placeholder="+56912345678" />

          <div className="flex gap-4">
            <div className="flex-1"><Field label="Región" name="region" placeholder="Tarapacá" /></div>
            <div className="flex-1"><Field label="Comuna" name="comuna" placeholder="Iquique" /></div>
          </div>

          <Field label="Dirección" name="address" placeholder="Av. Arturo Prat 123" />

          {/* Especialidad — solo para médicos */}
          {profile?.role === 'MEDICO' && (
            <Field label="Especialidad" name="especialidad" placeholder="Cardiología" />
          )}

          {/* Botones */}
          {isEditing && (
            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={handleCancel}
                className="px-4 py-2.5 text-sm font-medium text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={isSaving}
                className="px-4 py-2.5 text-sm font-medium bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded-lg transition-colors flex items-center gap-2"
              >
                {isSaving ? (
                  <>
                    <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                    </svg>
                    Guardando...
                  </>
                ) : 'Guardar cambios'}
              </button>
            </div>
          )}
        </form>
      </div>

      {/* Info alert */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-xl p-4">
        <div className="flex items-start gap-3">
          <svg className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div className="text-sm text-blue-800">
            <p className="font-medium mb-1">Información importante</p>
            <p className="text-blue-700">El RUT no puede ser modificado una vez registrado. Para cambios en datos críticos, contacta al administrador del sistema.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;