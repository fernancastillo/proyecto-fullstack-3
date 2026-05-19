import { useState, useEffect } from 'react';
import { REGIONES, COMUNAS_POR_REGION } from '../../data/chileData';
import { validateRutChileno } from '../../utils/rutValidator';

const PatientProfileForm = ({ patient, isEditing, onSave, onCancel, isSaving, allPatients = [] }) => {
  const [formData, setFormData] = useState({
    name: '',
    lastname: '',
    email: '',
    phone: '',
    rut: '',
    dv: '',
    region: '',
    comuna: '',
    address: ''
  });
  const [errors, setErrors] = useState({});
  const [availableComunas, setAvailableComunas] = useState([]);
  
  // Verificar si el RUT está pendiente (valor por defecto)
  const isRutPending = patient?.rut === 'PENDIENTE';

  useEffect(() => {
    if (patient) {
      setFormData({
        name: patient.name || '',
        lastname: patient.lastname || '',
        email: patient.email || '',
        phone: patient.phone || '',
        rut: patient.rut || '',
        dv: patient.dv || '',
        region: patient.region || '',
        comuna: patient.comuna || '',
        address: patient.address || ''
      });
      
      // Cargar comunas disponibles según la región del paciente
      if (patient.region) {
        const comunas = COMUNAS_POR_REGION[patient.region] || [];
        setAvailableComunas(comunas);
      }
    }
  }, [patient]);

  // Actualizar comunas cuando cambia la región
  useEffect(() => {
    if (formData.region) {
      const comunas = COMUNAS_POR_REGION[formData.region] || [];
      setAvailableComunas(comunas);
      
      // Si la comuna actual no pertenece a la nueva región, resetearla
      if (formData.comuna && !comunas.includes(formData.comuna)) {
        setFormData(prev => ({ ...prev, comuna: '' }));
      }
    } else {
      setAvailableComunas([]);
    }
  }, [formData.region]);

  // Función para seleccionar todo el texto al hacer foco
  const handleFocus = (e) => {
    e.target.select();
  };

  // Validar que el RUT sea único entre todos los pacientes
  const isRutUnique = (rut, currentPatientId) => {
    if (rut === 'PENDIENTE') return true; // RUT pendiente no valida unicidad
    
    return !allPatients.some(patient => 
      patient.rut === rut && patient.id !== currentPatientId
    );
  };

  // Validar que el teléfono sea único entre todos los pacientes
  const isPhoneUnique = (phone, currentPatientId) => {
    if (!phone || phone === '+56900000000') return true; // Teléfono por defecto no valida
    
    return !allPatients.some(patient => 
      patient.phone === phone && patient.id !== currentPatientId
    );
  };

  // Validar que el email sea único entre todos los pacientes
  const isEmailUnique = (email, currentPatientId) => {
    if (!email) return true;
    
    return !allPatients.some(patient => 
      patient.email === email && patient.id !== currentPatientId
    );
  };

  const handlePhoneChange = (e) => {
    let value = e.target.value;
    
    if (value === '') {
      value = '';
    } else {
      const numbers = value.replace(/\D/g, '');
      const limitedNumbers = numbers.slice(0, 9);
      value = limitedNumbers;
    }
    
    setFormData(prev => ({ ...prev, phone: value ? `+56${value}` : '' }));
    if (errors.phone) {
      setErrors(prev => ({ ...prev, phone: '' }));
    }
  };

  // Validar RUT (solo números, 7-8 dígitos, y algoritmo módulo 11)
  const validateRut = (rut, dv) => {
    if (rut === 'PENDIENTE' && dv === '0') {
      return true; // Permitir editar mientras esté pendiente
    }
    
    if (!rut) return false;
    // Validar que solo contenga números
    if (!/^\d+$/.test(rut)) return false;
    // Validar longitud entre 7 y 8 caracteres
    if (rut.length < 7 || rut.length > 8) return false;
    if (!dv) return false;
    if (!/^[0-9Kk]$/.test(dv)) return false;
    
    // Validar algoritmo del módulo 11 (RUT chileno)
    return validateRutChileno(rut, dv);
  };

  // Función para limpiar espacios y validar longitud mínima
  const trimAndValidateLength = (value, minLength, fieldName) => {
    const trimmed = value.trim();
    if (!trimmed) {
      return { isValid: false, error: `${fieldName} es obligatorio` };
    }
    if (trimmed.length < minLength) {
      return { isValid: false, error: `${fieldName} debe tener al menos ${minLength} caracteres` };
    }
    return { isValid: true, error: null };
  };

  const validateForm = () => {
    const newErrors = {};
    const currentPatientId = patient?.id;
    
    // Validar RUT (solo si no está pendiente o si se está editando)
    if (isEditing && (!formData.rut || formData.rut === 'PENDIENTE')) {
      if (!formData.rut || formData.rut === 'PENDIENTE') {
        newErrors.rut = 'El RUT es obligatorio';
      } else if (!/^\d+$/.test(formData.rut)) {
        newErrors.rut = 'El RUT debe contener solo números';
      } else if (formData.rut.length < 7 || formData.rut.length > 8) {
        newErrors.rut = 'El RUT debe tener entre 7 y 8 dígitos';
      }
    } else if (!validateRut(formData.rut, formData.dv)) {
      if (!formData.rut) {
        newErrors.rut = 'El RUT es obligatorio';
      } else if (!/^\d+$/.test(formData.rut)) {
        newErrors.rut = 'El RUT debe contener solo números';
      } else if (formData.rut.length < 7 || formData.rut.length > 8) {
        newErrors.rut = 'El RUT debe tener entre 7 y 8 dígitos';
      } else if (!validateRutChileno(formData.rut, formData.dv)) {
        newErrors.rut = 'El RUT ingresado no es válido (revise el dígito verificador)';
      }
    }
    
    // Validar DV
    if (formData.dv && !/^[0-9Kk]$/.test(formData.dv)) {
      newErrors.dv = 'DV debe ser número o K';
    }
    
    // Validar unicidad del RUT (si no está pendiente y es válido)
    if (formData.rut && formData.rut !== 'PENDIENTE' && validateRutChileno(formData.rut, formData.dv)) {
      if (!isRutUnique(formData.rut, currentPatientId)) {
        newErrors.rut = 'Este RUT ya está registrado por otro paciente';
      }
    }
    
    // Validar email
    if (!formData.email.trim()) {
      newErrors.email = 'El email es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Ingresa un email válido';
    }
    
    // Validar unicidad del email
    if (formData.email && !isEmailUnique(formData.email, currentPatientId)) {
      newErrors.email = 'Este correo electrónico ya está registrado por otro paciente';
    }
    
    // Validar teléfono
    if (!formData.phone || formData.phone === '+56') {
      newErrors.phone = 'El teléfono es obligatorio';
    } else if (formData.phone.length !== 12) {
      newErrors.phone = 'Debe tener 9 dígitos después del +56 (ej: +56912345678)';
    }
    
    // Validar unicidad del teléfono
    if (formData.phone && formData.phone !== '+56900000000' && !isPhoneUnique(formData.phone, currentPatientId)) {
      newErrors.phone = 'Este número de teléfono ya está registrado por otro paciente';
    }
    
    // Validar nombre (mínimo 3 caracteres, sin espacios al inicio/final)
    const nameValidation = trimAndValidateLength(formData.name, 3, 'El nombre');
    if (!nameValidation.isValid) {
      newErrors.name = nameValidation.error;
    }
    
    // Validar apellido (mínimo 2 caracteres, sin espacios al inicio/final)
    const lastnameValidation = trimAndValidateLength(formData.lastname, 2, 'El apellido');
    if (!lastnameValidation.isValid) {
      newErrors.lastname = lastnameValidation.error;
    }
    
    // Validar región
    if (!formData.region) {
      newErrors.region = 'La región es obligatoria';
    }
    
    // Validar comuna
    if (!formData.comuna) {
      newErrors.comuna = 'La comuna es obligatoria';
    } else if (availableComunas.length > 0 && !availableComunas.includes(formData.comuna)) {
      newErrors.comuna = 'Selecciona una comuna válida para esta región';
    }
    
    // Validar dirección (mínimo 5 caracteres, sin espacios al inicio/final)
    const addressValidation = trimAndValidateLength(formData.address, 5, 'La dirección');
    if (!addressValidation.isValid) {
      newErrors.address = addressValidation.error;
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  // Manejar cambio de RUT (solo permitir números y actualizar DV automáticamente)
  const handleRutChange = (e) => {
    let value = e.target.value;
    // Solo permitir números
    value = value.replace(/\D/g, '');
    // Limitar a 8 dígitos
    if (value.length > 8) {
      value = value.slice(0, 8);
    }
    setFormData(prev => ({ ...prev, rut: value }));
    if (errors.rut) {
      setErrors(prev => ({ ...prev, rut: '' }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onSave(formData);
    }
  };

  const inputBaseClass = "w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm placeholder-slate-300 bg-slate-50 transition-colors duration-150 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100";
  const readonlyClass = "bg-slate-100 text-slate-600 cursor-not-allowed";
  const selectBaseClass = "w-full px-4 py-2.5 rounded-lg border text-slate-800 text-sm bg-slate-50 transition-colors duration-150 outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-100";

  // Modo vista (solo lectura)
  if (!isEditing) {
    return (
      <div className="p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              RUT
            </label>
            <div className={`${inputBaseClass} ${readonlyClass} flex items-center`}>
              {patient.rut}-{patient.dv}
            </div>
          </div>
          
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              Email
            </label>
            <div className={`${inputBaseClass} ${readonlyClass}`}>
              {patient.email}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              Nombre
            </label>
            <div className={`${inputBaseClass} ${readonlyClass}`}>
              {patient.name}
            </div>
          </div>
          
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              Apellido
            </label>
            <div className={`${inputBaseClass} ${readonlyClass}`}>
              {patient.lastname}
            </div>
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Teléfono
          </label>
          <div className={`${inputBaseClass} ${readonlyClass}`}>
            {patient.phone}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              Región
            </label>
            <div className={`${inputBaseClass} ${readonlyClass}`}>
              {patient.region || 'No especificada'}
            </div>
          </div>
          
          <div>
            <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
              Comuna
            </label>
            <div className={`${inputBaseClass} ${readonlyClass}`}>
              {patient.comuna || 'No especificada'}
            </div>
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Dirección
          </label>
          <div className={`${inputBaseClass} ${readonlyClass}`}>
            {patient.address || 'No especificada'}
          </div>
        </div>
      </div>
    );
  }

  // Modo edición
  return (
    <form onSubmit={handleSubmit} className="p-6 space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            RUT (sin dígito verificador)
          </label>
          <input
            type="text"
            name="rut"
            value={formData.rut}
            onChange={handleRutChange}
            onFocus={handleFocus}
            disabled={isSaving || (!isRutPending && formData.rut !== 'PENDIENTE')}
            className={`${inputBaseClass} ${errors.rut ? 'border-red-400 bg-red-50' : 'border-slate-200'} ${(!isRutPending && formData.rut !== 'PENDIENTE') ? readonlyClass : ''}`}
            placeholder="Ej: 12345678"
          />
          {errors.rut && <p className="mt-1 text-xs text-red-500">{errors.rut}</p>}
          {isRutPending && (
            <p className="mt-1 text-xs text-blue-600">
              ⚠️ Completa tu RUT para finalizar tu registro (7-8 dígitos numéricos)
            </p>
          )}
          <p className="mt-1 text-xs text-slate-400">
            Ingresa solo los números (ej: 12345678)
          </p>
        </div>
        
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Dígito Verificador
          </label>
          <input
            type="text"
            name="dv"
            value={formData.dv}
            onChange={handleChange}
            onFocus={handleFocus}
            disabled={isSaving || (!isRutPending && formData.dv !== '0')}
            maxLength={1}
            className={`${inputBaseClass} ${errors.dv ? 'border-red-400 bg-red-50' : 'border-slate-200'} ${(!isRutPending && formData.dv !== '0') ? readonlyClass : ''}`}
            placeholder="Ej: 5 o K"
          />
          {errors.dv && <p className="mt-1 text-xs text-red-500">{errors.dv}</p>}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Email
          </label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            onFocus={handleFocus}
            disabled={true}
            className={`${inputBaseClass} ${readonlyClass}`}
            placeholder="ejemplo@email.com"
          />
          {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email}</p>}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Nombre
          </label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            onFocus={handleFocus}
            disabled={isSaving}
            className={`${inputBaseClass} ${errors.name ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
            placeholder="Ej: Juan"
          />
          {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name}</p>}
          <p className="mt-1 text-xs text-slate-400">Mínimo 3 caracteres</p>
        </div>
        
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Apellido
          </label>
          <input
            type="text"
            name="lastname"
            value={formData.lastname}
            onChange={handleChange}
            onFocus={handleFocus}
            disabled={isSaving}
            className={`${inputBaseClass} ${errors.lastname ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
            placeholder="Ej: Pérez"
          />
          {errors.lastname && <p className="mt-1 text-xs text-red-500">{errors.lastname}</p>}
          <p className="mt-1 text-xs text-slate-400">Mínimo 2 caracteres</p>
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
          Teléfono
        </label>
        <div className="relative">
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 text-sm font-medium bg-slate-100 px-2 py-0.5 rounded">
            +56
          </div>
          <input
            type="tel"
            name="phone"
            value={formData.phone ? formData.phone.replace('+56', '') : ''}
            onChange={handlePhoneChange}
            onFocus={handleFocus}
            disabled={isSaving}
            className={`${inputBaseClass} pl-16 ${errors.phone ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
            placeholder="912345678"
          />
        </div>
        {errors.phone && <p className="mt-1 text-xs text-red-500">{errors.phone}</p>}
        <p className="mt-1 text-xs text-slate-400">
          Ingresa los 9 dígitos de tu teléfono (ej: 912345678)
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Región
          </label>
          <select
            name="region"
            value={formData.region || ''}
            onChange={handleChange}
            disabled={isSaving}
            className={`${selectBaseClass} ${errors.region ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
          >
            <option value="">Selecciona una región</option>
            {REGIONES.map(region => (
              <option key={region.id} value={region.nombre}>
                {region.nombre}
              </option>
            ))}
          </select>
          {errors.region && <p className="mt-1 text-xs text-red-500">{errors.region}</p>}
        </div>
        
        <div>
          <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
            Comuna
          </label>
          <select
            name="comuna"
            value={formData.comuna || ''}
            onChange={handleChange}
            disabled={isSaving || !formData.region}
            className={`${selectBaseClass} ${errors.comuna ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
          >
            <option value="">
              {formData.region ? 'Selecciona una comuna' : 'Primero selecciona una región'}
            </option>
            {availableComunas.map(comuna => (
              <option key={comuna} value={comuna}>
                {comuna}
              </option>
            ))}
          </select>
          {errors.comuna && <p className="mt-1 text-xs text-red-500">{errors.comuna}</p>}
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1.5">
          Dirección
        </label>
        <input
          type="text"
          name="address"
          value={formData.address}
          onChange={handleChange}
          onFocus={handleFocus}
          disabled={isSaving}
          className={`${inputBaseClass} ${errors.address ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
          placeholder="Ej: Av. Principal 123"
        />
        {errors.address && <p className="mt-1 text-xs text-red-500">{errors.address}</p>}
        <p className="mt-1 text-xs text-slate-400">Mínimo 5 caracteres</p>
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 mt-6">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSaving}
          className="px-4 py-2 border border-slate-300 rounded-lg text-slate-700 hover:bg-slate-50 transition-colors text-sm font-medium disabled:opacity-50"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={isSaving}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors text-sm font-medium disabled:opacity-50 flex items-center gap-2"
        >
          {isSaving ? (
            <>
              <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Guardando...
            </>
          ) : (
            'Guardar Cambios'
          )}
        </button>
      </div>
    </form>
  );
};

export default PatientProfileForm;