import React, { useState, useEffect } from 'react';

function Dashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalEvents: 0,
    totalOrders: 0,
    totalRevenue: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Fetch all dashboard data
      const [usersRes, eventsRes, ordersRes] = await Promise.all([
        fetch('/api/users'),
        fetch('/api/events'),
        fetch('/api/orders')
      ]);

      const users = await usersRes.json();
      const events = await eventsRes.json();
      const orders = await ordersRes.json();

      const totalRevenue = orders
        .filter(order => order.status === 'CONFIRMED')
        .reduce((sum, order) => sum + order.totalAmount, 0);

      setStats({
        totalUsers: users.length || 0,
        totalEvents: events.length || 0,
        totalOrders: orders.length || 0,
        totalRevenue: totalRevenue
      });
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div className="dashboard-container">
      <div className="section-heading">
        <div>
          <h1>Admin Dashboard</h1>
          <p className="muted-text">At-a-glance metrics to keep services healthy.</p>
        </div>
      </div>
      
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-number">{stats.totalUsers}</div>
          <div className="stat-label">Total Users</div>
        </div>
        <div className="stat-card">
          <div className="stat-number">{stats.totalEvents}</div>
          <div className="stat-label">Total Events</div>
        </div>
        <div className="stat-card">
          <div className="stat-number">{stats.totalOrders}</div>
          <div className="stat-label">Total Orders</div>
        </div>
        <div className="stat-card">
          <div className="stat-number">${stats.totalRevenue.toFixed(2)}</div>
          <div className="stat-label">Total Revenue</div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>System Health</h3>
        </div>
        <div className="card-body">
          <p>All services are running normally.</p>
          <button 
            className="btn btn-primary"
            onClick={() => window.open('/api/services/health', '_blank')}
          >
            View Service Health
          </button>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
