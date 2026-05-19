import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-blue-700 text-white px-6 py-4 flex justify-between items-center">
      <span className="text-xl font-bold">RedNorte</span>
      <div className="flex gap-4 items-center">
        <Link to="/dashboard" className="hover:underline">Dashboard</Link>
        <Link to="/profile" className="hover:underline">Mi Perfil</Link>
        <Link to="/requests" className="hover:underline">Solicitudes</Link>
        <Link to="/waiting-list" className="hover:underline">Lista de Espera</Link>
        {user?.role === 'ADMIN' && (
          <Link to="/admin" className="hover:underline">Admin</Link>
        )}
        <button
          onClick={handleLogout}
          className="bg-white text-blue-700 px-3 py-1 rounded hover:bg-gray-100"
        >
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
};

export default Navbar;