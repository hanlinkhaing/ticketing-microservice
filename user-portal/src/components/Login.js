import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService, showErrorNotification } from '../utils/errorHandler';

function Login({ onLogin }) {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      console.log('[LOGIN] Starting login process:', { username: formData.username });
      
      const data = await apiService('/api/users/login', {
        method: 'POST',
        body: JSON.stringify(formData),
      }, 'user-service');

      console.log('[LOGIN] Login successful:', { username: formData.username });
      onLogin(data);
      navigate('/');
      
    } catch (error) {
      console.error('[LOGIN] Login failed:', error);
      
      // Use the enhanced error handling
      const errorMessage = showErrorNotification(error, 'user-service');
      setError(errorMessage);
      
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="card">
        <h2>Welcome Back</h2>
        <p className="muted-text">Access your tickets, chat with support, and track orders.</p>
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username:</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>
          <div className="form-group">
            <label>Password:</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        <p>
          Don't have an account? <a href="/register">Register here</a>
        </p>
      </div>
    </div>
  );
}

export default Login;
