package test.service;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import oris.model.api.AttendeeStatistics;
import oris.model.api.EventResult;
import oris.model.db.Attendee;
import oris.model.db.Event;
import oris.model.db.EventStatistics;
import oris.model.db.Result;
import oris.service.AttendeeService;
import oris.service.EventService;
import oris.service.StatisticsService;
import test.spring.BaseSpringTest;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatisticsServiceTest extends BaseSpringTest {

    public static final String ATTENDEE_EMPTY_RESULT = "STATS_SERVICE_0";
    public static final String ATTENDEE_SINGLE_RESULT = "STATS_SERVICE_1";
    public static final String ATTENDEE_MULTIPLE_RESULT = "STATS_SERVICE_2";

    private StatisticsService statisticsService;
    private EventService eventService;
    private AttendeeService attendeeService;

    @Autowired
    public StatisticsServiceTest(StatisticsService statisticsService, EventService eventService, AttendeeService attendeeService) {
        this.statisticsService = statisticsService;
        this.eventService = eventService;
        this.attendeeService = attendeeService;
    }

    @Test
    public void emptyResultSetTest() {
        AttendeeStatistics statistics = statisticsService.getAttendeeStatistics(ATTENDEE_EMPTY_RESULT);
        Assertions.assertEquals(ATTENDEE_EMPTY_RESULT, statistics.getRegNo());
        Assertions.assertTrue(statistics.getEventResults().isEmpty());
    }

    @Test
    public void oneResultTest() {
        AttendeeStatistics statistics = statisticsService.getAttendeeStatistics(ATTENDEE_SINGLE_RESULT);
        Assertions.assertEquals(ATTENDEE_SINGLE_RESULT, statistics.getRegNo());
        Assertions.assertEquals(1, statistics.getEventResults().size());

        EventResult eventResult = statistics.getEventResults().get(0);

        Assertions.assertEquals(1, eventResult.getAttendeesInCategory().intValue());
        Assertions.assertEquals("A1", eventResult.getCategory());
    }

    @Test
    public void multipleResultsTest() {
        AttendeeStatistics statistics = statisticsService.getAttendeeStatistics(ATTENDEE_MULTIPLE_RESULT);
        Assertions.assertEquals(ATTENDEE_MULTIPLE_RESULT, statistics.getRegNo());
        Assertions.assertEquals(3, statistics.getEventResults().size());
    }

    @BeforeAll
    public void setupData() {
        Attendee attendeeSingleEvent = attendeeService.findOrCreate(ATTENDEE_SINGLE_RESULT);
        Attendee attendeeMultipleEvents = attendeeService.findOrCreate(ATTENDEE_MULTIPLE_RESULT);

        setupEventWithOneResultEachAttendee(1000L, attendeeSingleEvent, attendeeMultipleEvents);
        setupEventWithOneResultEachAttendee(1001L, attendeeMultipleEvents);
        setupEventWithOneResultEachAttendee(1002L, attendeeMultipleEvents);
    }

    private void setupEventWithOneResultEachAttendee(Long eventId, Attendee... attendees) {
        final Event event = new Event();
        event.setEventId(eventId);
        event.setResults(Lists.newArrayList());

        EventStatistics eventStatistics = new EventStatistics();
        eventStatistics.setAttendees(1);
        eventStatistics.setAttendeesWithDisqualified(2);
        eventStatistics.setCategory("A1");
        eventStatistics.setEvent(event);

        event.setEventStatistics(List.of(eventStatistics));

        for (Attendee attendee : attendees) {
            Result result = new Result();
            result.setCategory("A1");
            result.setAttendee(attendee);
            result.setEvent(event);
            event.getResults().add(result);
        }

        eventService.save(event);
    }
}