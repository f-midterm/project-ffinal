import React from 'react';
import { Navigate } from 'react-router-dom';

/**
 * PrivateRoute component - Protects routes that require authentication
 * Redirects to signup page if no token is found
 * 
 * @param {object} props - Component props
 * @param {React.ReactNode} props.children - Child components to render if authenticated
 * @returns {React.ReactNode} Protected route or redirect
 */
const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  
  // If no token, redirect to signup page
  if (!token) {
    console.log('PrivateRoute: No token found, redirecting to signup');
    return <Navigate to="/signup" replace />;
  }
  
  // If token exists, render the protected component
  return children;
};

export default PrivateRoute;
