package oris.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oris.model.db.Event;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

    String TODAYS_EVENTS_WITHOUT_RESULTS_QUERY = "SELECT e FROM Event e WHERE e.results IS EMPTY AND e.date = :todaysDate";

    String PAST_EVENTS_WITHOUT_RESULTS_QUERY = "SELECT e FROM Event e WHERE e.results IS EMPTY AND e.date > :fromDate AND e.date < :toDate";

    @Query(TODAYS_EVENTS_WITHOUT_RESULTS_QUERY)
    Collection<Event> findAllTodaysEventsWithoutResults(@Param("todaysDate") final LocalDate now);

    @Query(PAST_EVENTS_WITHOUT_RESULTS_QUERY)
    Collection<Event> findAllEventsWithoutResultsBetweenDates(@Param("fromDate") final LocalDate fromDate, @Param("toDate") final LocalDate toDate);
}