package oris.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oris.model.api.EventResult;
import oris.model.db.Result;

import java.util.List;

@Repository
public interface ResultRepository extends CrudRepository<Result, Long> {

    String STATISTICS_ATTENDEE_QUERY = "SELECT new oris.model.api.EventResult(" +
            "r.event.eventId, r.event.name, r.place, es.attendees, es.attendeesWithDisqualified, r.time, r.loss, r.category, r.event.date) " +
            "FROM Result r JOIN r.event.eventStatistics es WHERE r.attendee.registrationNumber = :registrationNumber AND r.category = es.category";

    @Query(STATISTICS_ATTENDEE_QUERY)
    List<EventResult> getStatisticsForAttendee(@Param("registrationNumber") String registrationNumber);
}