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
      const response = await fetch('/api/orders/user/1'); // This should use actual user ID
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

  const handleCancelOrder = async (orderId) => {
    try {
      const response = await fetch(`/api/orders/${orderId}/cancel`, {
        method: 'PUT',
      });

      if (response.ok) {
        fetchOrders(); // Refresh orders
        alert('Order cancelled successfully');
      } else {
        alert('Failed to cancel order');
      }
    } catch (error) {
      alert('Error cancelling order');
    }
  };

  if (loading) return <div className="loading">Loading orders...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="orders-container">
      <h1>My Orders</h1>
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <div key={order.id} className="card">
              <div className="card-header">
                <h3>Order #{order.id}</h3>
                <span className={`status status-${order.status.toLowerCase()}`}>
                  {order.status}
                </span>
              </div>
              <div className="card-body">
                <p><strong>Event ID:</strong> {order.eventId}</p>
                <p><strong>Quantity:</strong> {order.ticketQuantity}</p>
                <p><strong>Total Amount:</strong> ${order.totalAmount}</p>
                <p><strong>Created:</strong> {new Date(order.createdAt).toLocaleDateString()}</p>
                
                {order.status === 'PENDING' && (
                  <button 
                    className="btn btn-danger"
                    onClick={() => handleCancelOrder(order.id)}
                  >
                    Cancel Order
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Orders;