// Base API configurations
const AUTH_API_BASE_URL = 'http://localhost:8081'; // Auth service URL
const USER_API_BASE_URL = 'http://localhost:8080'; // User service URL

// Helper function to get headers with authorization token
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

// Generic API call function
const apiCall = async (endpoint: string, options: RequestInit = {}, isAuthEndpoint: boolean = false) => {
  const baseURL = isAuthEndpoint ? AUTH_API_BASE_URL : USER_API_BASE_URL;
  const url = `${baseURL}${endpoint}`;

  const config: RequestInit = {
    headers: getAuthHeaders(),
    ...options
  };

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error(`API call failed for ${url}:`, error);
    throw error;
  }
};

// Auth API service
export const authApi = {
  // Login with email and password
  login: async (email: string, password: string) => {
    return apiCall('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }, true); // true indicates this is an auth endpoint
  },

  // Logout
  logout: async () => {
    return apiCall('/api/auth/logout', {
      method: 'POST'
    }, true); // true indicates this is an auth endpoint
  }
};

// User API service
export const userApi = {
  // Register a new user
  register: async (userData: {
    email: string;
    password: string;
    firstName: string;
    lastName?: string;
  }) => {
    return apiCall('/api/users/signup', {
      method: 'POST',
      body: JSON.stringify(userData)
    }, false); // false indicates this is a user endpoint
  },

  // Get current user profile
  getCurrentUser: async () => {
    return apiCall('/api/users/me', {
      method: 'GET'
    }, false); // false indicates this is a user endpoint
  },

  // Update user profile
  updateUser: async (userData: {
    firstName?: string;
    lastName?: string;
  }) => {
    return apiCall('/api/users/me', {
      method: 'PATCH',
      body: JSON.stringify(userData)
    }, false); // false indicates this is a user endpoint
  },

  // Delete user account
  deleteUser: async () => {
    return apiCall('/api/users/me', {
      method: 'DELETE'
    }, false); // false indicates this is a user endpoint
  },

  // Validate user credentials
  validateUser: async (email: string, password: string) => {
    return apiCall('/api/users/validate', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }, false); // false indicates this is a user endpoint
  },

  // Login method (for backward compatibility)
  login: async (email: string, password: string) => {
    return authApi.login(email, password);
  },

  // Logout method (for backward compatibility)
  logout: async () => {
    return authApi.logout();
  }
};

// Utility functions for token management
export const tokenService = {
  setToken: (token: string) => {
    localStorage.setItem('token', token);
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  removeToken: () => {
    localStorage.removeItem('token');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  }
};

export default {
  authApi,
  userApi,
  tokenService
};