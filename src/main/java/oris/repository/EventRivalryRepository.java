package oris.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import oris.model.db.EventRivalry;

import java.util.Collection;
import java.util.List;

@Repository
public interface EventRivalryRepository extends CrudRepository<EventRivalry, Long> {

    String DROP_INDEXES = "DROP INDEX idx_event_rivalry_category; " +
            "DROP INDEX idx_event_rivalry_event; " +
            "DROP INDEX idx_event_rivalry_attendee_rival; " +
            "DROP INDEX idx_event_rivalry_attendee;";

    String CREATE_INDEXES = "CREATE INDEX idx_event_rivalry_category " +
            "ON public.event_rivalries " +
            "USING btree " +
            "(category COLLATE pg_catalog.default); " +
            "CREATE INDEX idx_event_rivalry_event" +
            "  ON public.event_rivalries" +
            "  USING btree" +
            "  (event_id); " +
            "CREATE INDEX idx_event_rivalry_attendee_rival" +
            "  ON public.event_rivalries" +
            "  USING btree" +
            "  (attendee_id, rival_id); " +
            "CREATE INDEX idx_event_rivalry_attendee" +
            "  ON public.event_rivalries" +
            "  USING btree" +
            "  (attendee_id);";

    Collection<EventRivalry> findAllByAttendeeId(Long attendeeId);

    List<EventRivalry> findAllByAttendeeIdAndEventId(Long attendeeId, Collection<Long> eventIds);

    @Modifying
    @Transactional
    @Query(value = DROP_INDEXES, nativeQuery = true)
    void dropIndexes();

    @Modifying
    @Transactional
    @Query(value = CREATE_INDEXES, nativeQuery = true)
    void createIndexes();
}