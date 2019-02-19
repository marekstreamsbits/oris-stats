package oris.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oris.model.db.Event;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

    String TODAYS_EVENTS_WITHOUT_RESULTS_QUERY = "SELECT e FROM Event e WHERE e.results IS EMPTY AND e.date = :todaysDate";

    String PAST_EVENTS_WITHOUT_RESULTS_QUERY = "SELECT e FROM Event e WHERE e.results IS EMPTY AND e.date > :fromDate AND e.date < :toDate";

    String EVENTS_FOR_ATTENDEE_QUERY = "SELECT DISTINCT e FROM Event e WHERE e.id IN " +
            "((SELECT r.event.id FROM Result r WHERE r.attendee.registrationNumber = :registrationNumber)) ORDER BY e.date DESC";

    @Query(TODAYS_EVENTS_WITHOUT_RESULTS_QUERY)
    Collection<Event> findAllEventsWithoutResultsForDate(@Param("todaysDate") final LocalDate date);

    @Query(PAST_EVENTS_WITHOUT_RESULTS_QUERY)
    Collection<Event> findAllEventsWithoutResultsBetweenDates(@Param("fromDate") final LocalDate fromDate, @Param("toDate") final LocalDate toDate);

    @Query(EVENTS_FOR_ATTENDEE_QUERY)
    List<Event> findAllEventsForAttendee(@Param("registrationNumber") final String registrationNumber);

    Event findByEventId(final Long eventId);
}