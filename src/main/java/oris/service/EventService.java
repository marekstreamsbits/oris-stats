package oris.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.repository.EventRepository;

@Service
public class EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}