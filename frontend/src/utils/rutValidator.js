/**
 * Valida un RUT chileno usando el algoritmo del módulo 11
 * @param {string} rut - El RUT sin dígito verificador (solo números)
 * @param {string} dv - El dígito verificador esperado
 * @returns {boolean} - true si el RUT es válido, false si no
 */
export const validateRutChileno = (rut, dv) => {
  // Limpiar y validar entrada
  if (!rut || !dv) return false;
  
  // Convertir rut a string y limpiar
  let rutStr = String(rut).replace(/\./g, '').trim();
  let dvStr = String(dv).trim().toUpperCase();
  
  // Validar que el rut solo contenga números
  if (!/^\d+$/.test(rutStr)) return false;
  
  // Validar longitud del rut (entre 7 y 8 dígitos)
  if (rutStr.length < 7 || rutStr.length > 8) return false;
  
  // Validar que el DV sea un número o K
  if (!/^[0-9K]$/.test(dvStr)) return false;
  
  // Algoritmo del módulo 11
  let suma = 0;
  let multiplicador = 2;
  
  // Recorrer el rut de derecha a izquierda
  for (let i = rutStr.length - 1; i >= 0; i--) {
    suma += parseInt(rutStr.charAt(i)) * multiplicador;
    multiplicador++;
    if (multiplicador > 7) {
      multiplicador = 2;
    }
  }
  
  // Calcular dígito verificador esperado
  const resto = suma % 11;
  const dvEsperado = resto === 1 ? 'K' : (11 - resto).toString();
  const dvCalculado = resto === 0 ? '0' : dvEsperado;
  
  // Comparar con el DV ingresado
  return dvCalculado === dvStr;
};

/**
 * Formatea un RUT con puntos y guión
 * @param {string} rut - El RUT sin formato
 * @param {string} dv - El dígito verificador
 * @returns {string} - RUT formateado (ej: 12.345.678-5)
 */
export const formatRut = (rut, dv) => {
  if (!rut) return '';
  
  let rutStr = String(rut).replace(/\./g, '');
  
  // Formatear con puntos
  let formatted = '';
  for (let i = rutStr.length; i > 0; i -= 3) {
    if (formatted) formatted = '.' + formatted;
    formatted = rutStr.substring(Math.max(0, i - 3), i) + formatted;
  }
  
  return `${formatted}-${dv}`;
};

/**
 * Limpia un RUT (elimina puntos, guiones y espacios)
 * @param {string} rut - El RUT con formato
 * @returns {object} - { rut: string, dv: string }
 */
export const cleanRut = (rut) => {
  if (!rut) return { rut: '', dv: '' };
  
  // Eliminar puntos, guiones y espacios
  let cleaned = rut.replace(/[.\-\s]/g, '');
  
  // Separar RUT y DV
  if (cleaned.length > 1) {
    const dv = cleaned.slice(-1);
    const rutNumber = cleaned.slice(0, -1);
    return { rut: rutNumber, dv: dv.toUpperCase() };
  }
  
  return { rut: cleaned, dv: '' };
};