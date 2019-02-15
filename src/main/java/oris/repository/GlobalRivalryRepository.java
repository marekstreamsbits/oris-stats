package oris.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import oris.model.api.GlobalRivalryDto;
import oris.model.db.GlobalRivalry;

import java.util.Collection;
import java.util.List;

@Repository
public interface GlobalRivalryRepository extends CrudRepository<GlobalRivalry, Long> {

    String DROP_INDEXES = "DROP INDEX idx_global_rivalries_category; " +
            "DROP INDEX idx_global_rivalries_events_count;" +
            "DROP INDEX idx_global_rivalries_win_difference;" +
            "DROP INDEX idx_global_rivalries_win_difference_abs;" +
            "DROP INDEX idx_global_rivalries_attendee;" +
            "DROP INDEX idx_global_rivalries_attendee_rival;";

    String CREATE_INDEXES = "CREATE INDEX idx_global_rivalries_category" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (category COLLATE pg_catalog.default);" +
            "CREATE INDEX idx_global_rivalries_event_count" +
            "  ON public.global_rivalries" +
            "  USING btree" +
            "  (event_count);" +
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

    String FIND_ALL_BY_ATTENDEE_REGISTRATION_NUMBER = "SELECT new oris.model.api.GlobalRivalryDto(gr.attendee.registrationNumber," +
            " gr.rival.registrationNumber, gr.category, gr.eventCount, gr.winDifference)" +
            " FROM GlobalRivalry gr WHERE gr.attendee.registrationNumber =:registrationNumber AND" +
            " LENGTH(gr.rival.registrationNumber) > 4 AND gr.rival.registrationNumber not like 'NN%'" +
            " ORDER BY gr.eventCount DESC, gr.winDifferenceAbs ASC";

    String FIND_ALL_BY_ATTENDEE_REGISTRATION_NUMBER_AND_RIVAL_REGISTRATION_NUMBER = "SELECT new oris.model.api.GlobalRivalryDto(gr.attendee.registrationNumber," +
            " gr.rival.registrationNumber, gr.category, gr.eventCount, gr.winDifference)" +
            " FROM GlobalRivalry gr WHERE gr.attendee.registrationNumber =:registrationNumber AND gr.rival.registrationNumber =:rivalRegistrationNumber" +
            " ORDER BY gr.eventCount DESC, gr.winDifferenceAbs ASC";

    @Transactional
    @Modifying
    @Query(value = DROP_INDEXES, nativeQuery = true)
    void dropIndexes();

    @Transactional
    @Modifying
    @Query(value = CREATE_INDEXES, nativeQuery = true)
    void createIndexes();

    Collection<GlobalRivalry> findAllByAttendeeIdAndRivalIdAndCategory(Long attendeeId, List<Long> rivalIds, String category);

    @Query(value = FIND_ALL_BY_ATTENDEE_REGISTRATION_NUMBER)
    List<GlobalRivalryDto> findAllByAttendeeRegistrationNumber(@Param("registrationNumber") String registrationNumber);

    @Query(value = FIND_ALL_BY_ATTENDEE_REGISTRATION_NUMBER_AND_RIVAL_REGISTRATION_NUMBER)
    List<GlobalRivalryDto> findAllByAttendeeRegistrationNumberAndRivalRegistrationNumber(@Param("registrationNumber") String registrationNumber, @Param("rivalRegistrationNumber") String rivalRegistrationNumber);
}