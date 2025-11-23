import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { login, logout, getCurrentUser } from '../api/auth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = getCurrentUser();
    if (storedUser) {
      setUser(storedUser);
    }
    setLoading(false);
  }, []);

  const signIn = useCallback(async (username, password) => {
    const userData = await login(username, password);
    setUser(userData);
    return userData;
  }, []);

  const signOut = useCallback(() => {
    logout();
    setUser(null);
  }, []);

  const value = {
    user,
    loading,
    signIn,
    signOut,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'admin'
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);