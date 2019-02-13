package oris.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.repository.EventRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

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

    public Collection<Event> getTodaysEventsWithoutResults() {
        return eventRepository.findAllTodaysEventsWithoutResults(LocalDate.now());
    }

    public Collection<Event> getPastMonthEventsWithoutResults() {
        final LocalDate monthAgo = LocalDate.now().minus(1, ChronoUnit.MONTHS);
        final LocalDate today = LocalDate.now();
        return eventRepository.findAllEventsWithoutResultsBetweenDates(monthAgo, today);
    }
}