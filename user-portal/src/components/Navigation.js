import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

function Navigation({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        Ticket System
      </div>
      <ul className="navbar-nav">
        <li>
          <Link to="/" className="nav-link">Events</Link>
        </li>
        <li>
          <Link to="/orders" className="nav-link">My Orders</Link>
        </li>
        <li>
          <Link to="/chat" className="nav-link">Support</Link>
        </li>
        <li>
          <Link to="/profile" className="nav-link">Profile</Link>
        </li>
        <li>
          <button onClick={handleLogout} className="btn btn-danger">
            Logout
          </button>
        </li>
      </ul>
    </nav>
  );
}

export default Navigation;