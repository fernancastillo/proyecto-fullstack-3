import { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

const loadFromStorage = () => {
  try {
    const user  = localStorage.getItem('auth_user');
    const token = localStorage.getItem('auth_token');
    return {
      user:  user  ? JSON.parse(user) : null,
      token: token ?? null,
    };
  } catch {
    return { user: null, token: null };
  }
};

export const AuthProvider = ({ children }) => {
  const stored = loadFromStorage();
  const [user,  setUser]  = useState(stored.user);
  const [token, setToken] = useState(stored.token);

  /**
   * Guarda el usuario y el token JWT tras login o registro exitoso.
   * @param {object} userData - { id, email, role, name, lastname, especialidad }
   * @param {string} jwtToken - token JWT generado por el BFF
   */
  const login = (userData, jwtToken) => {
    setUser(userData);
    setToken(jwtToken);
    localStorage.setItem('auth_user',  JSON.stringify(userData));
    localStorage.setItem('auth_token', jwtToken);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_token');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
