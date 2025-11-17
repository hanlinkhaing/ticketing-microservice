import React from 'react';
import { Link } from 'react-router-dom';

function Navigation({ user, onLogout }) {
  return (
    <nav className="navbar">
      <div className="navbar-brand">
        Admin Portal - Ticket System
      </div>
      <ul className="navbar-nav">
        <li>
          <Link to="/" className="nav-link">Dashboard</Link>
        </li>
        <li>
          <Link to="/users" className="nav-link">Users</Link>
        </li>
        <li>
          <Link to="/events" className="nav-link">Events</Link>
        </li>
        <li>
          <Link to="/orders" className="nav-link">Orders</Link>
        </li>
        <li>
          <Link to="/chat" className="nav-link">Support Chat</Link>
        </li>
        <li>
          <button onClick={onLogout} className="btn btn-danger">
            Logout
          </button>
        </li>
      </ul>
    </nav>
  );
}

export default Navigation;