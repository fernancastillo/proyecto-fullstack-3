import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ProfilePage from './pages/ProfilePage';
import RequestsPage from './pages/RequestsPage';
import WaitingListPage from './pages/WaitingListPage';
import AdminPage from './pages/AdminPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/*" element={
            <>
              <Navbar />
              <div className="p-6">
                <Routes>
                  <Route path="/dashboard" element={
                    <ProtectedRoute><DashboardPage /></ProtectedRoute>
                  } />
                  <Route path="/profile" element={
                    <ProtectedRoute><ProfilePage /></ProtectedRoute>
                  } />
                  <Route path="/requests" element={
                    <ProtectedRoute><RequestsPage /></ProtectedRoute>
                  } />
                  <Route path="/waiting-list" element={
                    <ProtectedRoute><WaitingListPage /></ProtectedRoute>
                  } />
                  <Route path="/admin" element={
                    <ProtectedRoute adminOnly={true}><AdminPage /></ProtectedRoute>
                  } />
                  <Route path="/" element={<Navigate to="/dashboard" />} />
                </Routes>
              </div>
            </>
          } />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;