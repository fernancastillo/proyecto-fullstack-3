import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const DashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      navigate('/admin', { replace: true });
    }
  }, [user, navigate]);

  if (user?.role === 'ADMIN') return null;

  return (
    <div>
      <h1 className="text-2xl font-bold text-blue-700 mb-4">Dashboard</h1>
      <p className="text-gray-500">Bienvenido al sistema RedNorte.</p>
    </div>
  );
};

export default DashboardPage;