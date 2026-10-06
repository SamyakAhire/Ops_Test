import React from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

const API = 'http://localhost:5000/api/events';

function App() {
  const [events, setEvents] = React.useState([]);
  const [selectedDate, setSelectedDate] = React.useState('2026-10-07');
  const [title, setTitle] = React.useState('');
  const [time, setTime] = React.useState('09:00');
  const [message, setMessage] = React.useState('');

  const loadEvents = React.useCallback(async () => {
    try {
      const response = await fetch(API);
      setEvents(await response.json());
    } catch {
      setMessage('Backend is not running. Start the Node server.');
    }
  }, []);

  React.useEffect(() => { loadEvents(); }, [loadEvents]);

  const addEvent = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    const response = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, date: selectedDate, time })
    });

    if (response.ok) {
      setTitle('');
      setMessage('Event added successfully.');
      loadEvents();
    }
  };

  const deleteEvent = async (id) => {
    await fetch(`${API}/${id}`, { method: 'DELETE' });
    loadEvents();
  };

  const days = Array.from({ length: 31 }, (_, i) => i + 1);
  const month = '2026-10';

  return (
    <main className="page">
      <section className="calendar-card">
        <header>
          <div>
            <p className="eyebrow">REACT + NODE TEST</p>
            <h1>October 2026</h1>
          </div>
          <span className="status">API Connected</span>
        </header>

        <div className="weekdays">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => <b key={day}>{day}</b>)}
        </div>

        <div className="grid">
          {days.map(day => {
            const date = `${month}-${String(day).padStart(2, '0')}`;
            const dayEvents = events.filter(event => event.date === date);
            return (
              <button className={`day ${selectedDate === date ? 'selected' : ''}`} key={date} onClick={() => setSelectedDate(date)}>
                <strong>{day}</strong>
                {dayEvents.map(event => <small key={event.id}>{event.time} {event.title}</small>)}
              </button>
            );
          })}
        </div>
      </section>

      <aside className="panel">
        <p className="eyebrow">NEW EVENT</p>
        <h2>{selectedDate}</h2>
        <form onSubmit={addEvent}>
          <label>Event title<input value={title} onChange={e => setTitle(e.target.value)} placeholder="e.g. Team Meeting" /></label>
          <label>Time<input type="time" value={time} onChange={e => setTime(e.target.value)} /></label>
          <button className="add" type="submit">Add Event</button>
        </form>
        {message && <p className="message">{message}</p>}

        <h3>Events</h3>
        <div className="events">
          {events.filter(e => e.date === selectedDate).map(event => (
            <div className="event" key={event.id}>
              <div><b>{event.title}</b><span>{event.time}</span></div>
              <button onClick={() => deleteEvent(event.id)}>Delete</button>
            </div>
          ))}
          {!events.some(e => e.date === selectedDate) && <p className="empty">No events for this date.</p>}
        </div>
      </aside>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
