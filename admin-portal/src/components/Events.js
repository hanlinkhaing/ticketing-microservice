import React, { useState, useEffect } from 'react';

function Events() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    eventDate: '',
    venue: '',
    totalTickets: '',
    ticketPrice: ''
  });

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await fetch('/api/events/user/1'); // This should use actual admin ID
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch('/api/events', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': '1' // This should come from auth context
        },
        body: JSON.stringify({
          ...formData,
          totalTickets: parseInt(formData.totalTickets),
          ticketPrice: parseFloat(formData.ticketPrice)
        }),
      });

      if (response.ok) {
        fetchEvents();
        setShowForm(false);
        setFormData({
          name: '',
          description: '',
          eventDate: '',
          venue: '',
          totalTickets: '',
          ticketPrice: ''
        });
      } else {
        alert('Failed to create event');
      }
    } catch (error) {
      alert('Error creating event');
    }
  };

  if (loading) return <div className="loading">Loading events...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="events-container">
      <h1>Event Management</h1>
      
      <div className="card">
        <div className="card-header">
          <h3>My Events ({events.length})</h3>
          <button 
            className="btn btn-success"
            onClick={() => setShowForm(!showForm)}
          >
            {showForm ? 'Cancel' : 'Create Event'}
          </button>
        </div>
        
        {showForm && (
          <div className="event-form">
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Event Name:</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>Description:</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  className="form-control"
                  rows="3"
                  required
                />
              </div>
              <div className="form-group">
                <label>Event Date:</label>
                <input
                  type="datetime-local"
                  name="eventDate"
                  value={formData.eventDate}
                  onChange={(e) => setFormData({...formData, eventDate: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>Venue:</label>
                <input
                  type="text"
                  name="venue"
                  value={formData.venue}
                  onChange={(e) => setFormData({...formData, venue: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>Total Tickets:</label>
                <input
                  type="number"
                  name="totalTickets"
                  value={formData.totalTickets}
                  onChange={(e) => setFormData({...formData, totalTickets: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>Ticket Price:</label>
                <input
                  type="number"
                  step="0.01"
                  name="ticketPrice"
                  value={formData.ticketPrice}
                  onChange={(e) => setFormData({...formData, ticketPrice: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary">
                Create Event
              </button>
            </form>
          </div>
        )}
        
        <div className="events-list">
          {events.length === 0 ? (
            <p>No events found.</p>
          ) : (
            <div className="grid">
              {events.map(event => (
                <div key={event.id} className="card">
                  <div className="card-header">
                    <h4>{event.name}</h4>
                    <span className={`status status-${event.status.toLowerCase()}`}>
                      {event.status}
                    </span>
                  </div>
                  <div className="card-body">
                    <p><strong>Date:</strong> {new Date(event.eventDate).toLocaleDateString()}</p>
                    <p><strong>Venue:</strong> {event.venue}</p>
                    <p><strong>Price:</strong> ${event.ticketPrice}</p>
                    <p><strong>Available:</strong> {event.availableTickets}/{event.totalTickets}</p>
                    <p>{event.description}</p>
                    <button className="btn btn-warning btn-sm">Edit</button>
                    <button className="btn btn-danger btn-sm">Delete</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Events;