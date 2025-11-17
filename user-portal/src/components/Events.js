import React, { useState, useEffect } from 'react';

function Events() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await fetch('/api/events/active');
      const data = await response.json();
      
      if (response.ok) {
        setEvents(data);
      } else {
        setError('Failed to fetch events');
      }
    } catch (error) {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const handlePurchase = async (eventId, ticketPrice) => {
    try {
      // Create order
      const orderResponse = await fetch('/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: 1, // This should come from auth context
          eventId: eventId,
          ticketQuantity: 1,
          totalAmount: ticketPrice
        }),
      });

      const orderData = await orderResponse.json();

      if (orderResponse.ok) {
        alert('Order created successfully! Proceed to payment.');
      } else {
        alert('Failed to create order');
      }
    } catch (error) {
      alert('Error creating order');
    }
  };

  if (loading) return <div className="loading">Loading events...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="events-container">
      <div className="section-heading">
        <div>
          <h1>Available Events</h1>
          <p className="muted-text">Choose an event and secure your seat in a few clicks.</p>
        </div>
      </div>
      {events.length === 0 ? (
        <p>No events available at the moment.</p>
      ) : (
        <div className="grid">
          {events.map(event => (
            <div key={event.id} className="card">
              <div className="card-header">
                <h3>{event.name}</h3>
              </div>
              <div className="card-body">
                <p><strong>Date:</strong> {new Date(event.eventDate).toLocaleDateString()}</p>
                <p><strong>Venue:</strong> {event.venue}</p>
                <p><strong>Price:</strong> ${event.ticketPrice}</p>
                <p><strong>Available Tickets:</strong> {event.availableTickets}</p>
                <p>{event.description}</p>
                <button 
                  className="btn btn-primary"
                  onClick={() => handlePurchase(event.id, event.ticketPrice)}
                  disabled={event.availableTickets === 0}
                >
                  {event.availableTickets === 0 ? 'Sold Out' : 'Purchase Ticket'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Events;
