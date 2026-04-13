import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, userApi, tokenService } from '../services/api';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (userData: { email: string; password: string; firstName: string; lastName?: string }) => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated on app load
    const initializeAuth = async () => {
      if (tokenService.isAuthenticated()) {
        try {
          const userData = await userApi.getCurrentUser();
          setUser(userData);
        } catch (error) {
          // If token is invalid, remove it
          tokenService.removeToken();
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const response = await authApi.login(email, password);

      if (response.success) {
        tokenService.setToken(response.token);

        // Fetch user data
        const userData = await userApi.getCurrentUser();
        setUser(userData);
      } else {
        throw new Error(response.message || 'Login failed');
      }
    } catch (error) {
      // Remove token if login fails
      tokenService.removeToken();
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      // Ignore logout errors
      console.error('Logout error:', error);
    } finally {
      tokenService.removeToken();
      setUser(null);
    }
  };

  const register = async (userData: { email: string; password: string; firstName: string; lastName?: string }) => {
    try {
      const response = await userApi.register(userData);
      setUser(response);
    } catch (error) {
      throw error;
    }
  };

  const value = {
    user,
    isAuthenticated: !!user,
    login,
    logout,
    register,
    loading
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};