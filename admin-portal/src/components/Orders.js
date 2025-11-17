import React, { useState, useEffect } from 'react';

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await fetch('/api/orders');
      const data = await response.json();
      
      if (response.ok) {
        setOrders(data);
      } else {
        setError('Failed to fetch orders');
      }
    } catch (error) {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      const endpoint = newStatus === 'CONFIRMED' ? 'confirm' : 'cancel';
      const response = await fetch(`/api/orders/${orderId}/${endpoint}`, {
        method: 'PUT',
      });

      if (response.ok) {
        fetchOrders(); // Refresh orders
      } else {
        alert('Failed to update order status');
      }
    } catch (error) {
      alert('Error updating order status');
    }
  };

  if (loading) return <div className="loading">Loading orders...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="orders-container">
      <h1>Order Management</h1>
      
      <div className="card">
        <div className="card-header">
          <h3>All Orders ({orders.length})</h3>
        </div>
        <div className="card-body">
          {orders.length === 0 ? (
            <p>No orders found.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>User ID</th>
                  <th>Event ID</th>
                  <th>Quantity</th>
                  <th>Total Amount</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.userId}</td>
                    <td>{order.eventId}</td>
                    <td>{order.ticketQuantity}</td>
                    <td>${order.totalAmount}</td>
                    <td>
                      <span className={`status status-${order.status.toLowerCase()}`}>
                        {order.status}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      {order.status === 'PENDING' && (
                        <>
                          <button 
                            className="btn btn-success btn-sm"
                            onClick={() => handleUpdateStatus(order.id, 'CONFIRMED')}
                          >
                            Confirm
                          </button>
                          <button 
                            className="btn btn-danger btn-sm"
                            onClick={() => handleUpdateStatus(order.id, 'CANCELLED')}
                          >
                            Cancel
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default Orders;