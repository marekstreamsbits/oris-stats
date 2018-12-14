package oris.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oris.model.db.EventRivalry;

import java.util.Collection;
import java.util.List;

@Repository
public interface EventRivalryRepository extends CrudRepository<EventRivalry, Long> {

    Collection<EventRivalry> findAllByAttendeeId(Long attendeeId);

    List<EventRivalry> findAllByAttendeeIdAndEventId(Long attendeeId, Collection<Long> eventIds);
}