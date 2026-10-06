import express from 'express';
import cors from 'cors';

const app = express();
const PORT = 5000;

app.use(cors());
app.use(express.json());

let events = [
  { id: 1, title: 'Team Meeting', date: '2026-10-07', time: '10:00' },
  { id: 2, title: 'Project Testing', date: '2026-10-09', time: '14:00' }
];

app.get('/api/events', (req, res) => {
  res.json(events);
});

app.post('/api/events', (req, res) => {
  const { title, date, time } = req.body;

  if (!title || !date) {
    return res.status(400).json({ message: 'Title and date are required.' });
  }

  const event = {
    id: Date.now(),
    title,
    date,
    time: time || '09:00'
  };

  events.push(event);
  res.status(201).json(event);
});

app.delete('/api/events/:id', (req, res) => {
  const id = Number(req.params.id);
  events = events.filter((event) => event.id !== id);
  res.json({ message: 'Event deleted.' });
});

app.listen(PORT, () => {
  console.log(`Calendar API running at http://localhost:${PORT}`);
});
