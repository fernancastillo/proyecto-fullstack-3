import axios from 'axios';
import logger from '../utils/logger';

const api = axios.create({
  baseURL: '/bff',
  timeout: 10000, 
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    logger.info(`${response.config.method.toUpperCase()} ${response.config.url} → ${response.status}`);
    return response;
  },
  (error) => {
    const status = error.response?.status ?? 'sin respuesta';
    const url = error.config?.url ?? 'desconocida';
    logger.error(`${error.config?.method?.toUpperCase()} ${url} → ${status}`, error.response?.data);
    return Promise.reject(error);
  }
);

export default api;