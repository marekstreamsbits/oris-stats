package oris.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oris.model.api.EventResult;
import oris.model.db.Event;
import oris.model.db.EventStatistics;
import oris.model.db.Result;
import oris.repository.EventRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static oris.model.api.EventResult.RESULT_COMPARATOR_BY_PLACE;

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

    public List<Event> findEventsForAttendee(final String registrationNumber) {
        return eventRepository.findAllEventsForAttendee(registrationNumber);
    }

    /**
     * @return map where key is the category
     */
    public Map<String, List<EventResult>> getResultsForEvent(final Long eventId) {
        final Event event = eventRepository.findByEventId(eventId);
        if (event == null) {
            return Map.of();
        }
        final Collection<Result> results = event.getResults();
        final Map<String, List<EventResult>> resultsMap = new HashMap<>();
        results.forEach(result -> {
            if (resultsMap.containsKey(result.getCategory())) {
                resultsMap.get(result.getCategory()).add(resolveEventResult(result, event));
            } else {
                resultsMap.put(result.getCategory(), new ArrayList<>(List.of(resolveEventResult(result, event))));
            }
        });
        resultsMap.values().forEach(resultsList ->
                Collections.sort(resultsList, RESULT_COMPARATOR_BY_PLACE));

        return resultsMap;
    }

    private EventResult resolveEventResult(final Result result, final Event event) {
        EventStatistics stats = null;
        for (final EventStatistics statistics : event.getEventStatistics()) {
            if (result.getCategory().equals(statistics.getCategory())) {
                stats = statistics;
                break;
            }
        }
        return new EventResult(event.getEventId(),
                event.getName(), result.getPlace(), stats.getAttendees(), stats.getAttendeesWithDisqualified(),
                result.getTime(), result.getLoss(), result.getCategory(), event.getDate());
    }
}