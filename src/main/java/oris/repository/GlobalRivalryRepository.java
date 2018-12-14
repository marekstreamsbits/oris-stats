package oris.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import oris.model.db.GlobalRivalry;

import java.util.Collection;
import java.util.List;

@Repository
public interface GlobalRivalryRepository extends CrudRepository<GlobalRivalry, Long> {

    String DROP_INDEXES = "DROP INDEX idx_global_rivalries_category; " +
            "DROP INDEX idx_global_rivalries_events_count; " +
            "DROP INDEX idx_global_rivalries_win_difference; " +
            "DROP INDEX idx_global_rivalries_win_difference_abs;" +
            "DROP INDEX idx_global_rivalries_attendee;" +
            "DROP INDEX idx_global_rivalries_attendee_rival;";

    String CREATE_INDEXES = "CREATE INDEX idx_global_rivalries_category" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (category COLLATE pg_catalog.default);" +
            "CREATE INDEX idx_global_rivalries_events_count" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (events_count);" +
            "CREATE INDEX idx_global_rivalries_win_difference" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (win_difference);" +
            "CREATE INDEX idx_global_rivalries_win_difference_abs" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (win_difference_abs);" +
            "CREATE INDEX idx_global_rivalries_attendee" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (attendee_id);" +
            "CREATE INDEX idx_global_rivalries_attendee_rival" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (attendee_id, rival_id);";

    @Transactional
    @Modifying
    @Query(value = DROP_INDEXES, nativeQuery = true)
    void dropIndexes();

    @Transactional
    @Modifying
    @Query(value = CREATE_INDEXES, nativeQuery = true)
    void createIndexes();

    Collection<GlobalRivalry> findAllByAttendeeIdAndRivalId(Long attendeeId, List<Long> rivalIds);

    Collection<GlobalRivalry> findAllByAttendeeIdAndRivalIdAndCategory(Long attendeeId, List<Long> rivalIds, String category);
}