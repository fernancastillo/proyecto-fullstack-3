import axios from 'axios';

const api = axios.create({
  baseURL: '/bff',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Adjunta el token JWT a cada request si existe en localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
