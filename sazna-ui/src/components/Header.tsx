import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Header.css';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      navigate('/');
    }
  };

  return (
    <header className="header">
      <div className="container header-container">
        <div>
          <Link to="/" className="logo-link">
            <span className="logo-circle">S</span>
            Sazna
          </Link>
        </div>

        <nav>
          <ul className="nav-list">
            <li>
              <Link to="/" className="nav-link">Home</Link>
            </li>
            <li>
              <Link to="/dashboard" className="nav-link">Dashboard</Link>
            </li>
            {!isAuthenticated ? (
              <>
                <li>
                  <Link to="/login" className="btn btn-outline">
                    Login
                  </Link>
                </li>
                <li>
                  <Link to="/signup" className="btn btn-primary">
                    Sign Up
                  </Link>
                </li>
              </>
            ) : (
              <li>
                <button
                  onClick={handleLogout}
                  className="logout-button"
                >
                  Logout
                </button>
              </li>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
};

export default Header;