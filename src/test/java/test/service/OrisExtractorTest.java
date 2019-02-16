package test.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import oris.extractor.response.ResultDTO;
import oris.model.db.Event;
import oris.model.db.EventLite;
import oris.service.AttendeeService;
import oris.service.EventService;
import oris.service.OrisApiExtractionService;
import oris.service.OrisExtractionService;
import test.spring.BaseSpringTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class OrisExtractorTest extends BaseSpringTest {

    private final OrisExtractionService orisExtractionService;

    @Autowired
    public OrisExtractorTest(AttendeeService attendeeService, EventService eventService, ExecutorService executorService) {
        this.orisExtractionService = new OrisExtractionService(attendeeService, executorService, eventService, orisApiExtractionServiceMock());
    }

    @Test
    public void extractAndPersist() {
        Collection<Event> persistedEvents = orisExtractionService.extractAndPersistEventDataWithResults(LocalDate.now(), LocalDate.now());

        persistedEvents.forEach(event -> {
            if (event.getEventId() == 1L) {
                verifyNoDisqualifiedEvent(event);
            } else {
                verifyDisqualifiedEvent(event);
            }
        });
    }

    private void verifyNoDisqualifiedEvent(Event event) {
        Assertions.assertEquals(event.getName(),"Event1");
        event.getResults().forEach(result -> {
            if ("A1".equals(result.getCategory())) {
                Assertions.assertEquals(60, result.getLoss().intValue());
                Assertions.assertEquals(2, result.getPlace().intValue());
                Assertions.assertEquals(132, result.getTime().intValue());
                Assertions.assertEquals("B3Q8D5A", result.getAttendee().getRegistrationNumber());
            } else {
                Assertions.assertNull(result.getLoss());
                Assertions.assertEquals(1, result.getPlace().intValue());
                Assertions.assertEquals(72, result.getTime().intValue());
                Assertions.assertEquals("T3Q8D5A", result.getAttendee().getRegistrationNumber());
            }
        });
        verifyEventStatistics(event, 1, 1, 1, 1);
    }

    private void verifyEventStatistics(Event event, int a1Attendees, int a1AttendeesWithDisq, int a2Attendees, int a2AttendeesWithDisq) {
        event.getEventStatistics().forEach(eventStatistics -> {
            if ("A1".equals(eventStatistics.getCategory())) {
                Assertions.assertEquals(a1Attendees, eventStatistics.getAttendees());
                Assertions.assertEquals(a1AttendeesWithDisq, eventStatistics.getAttendeesWithDisqualified());
            } else if ("A2".equals(eventStatistics.getCategory())) {
                Assertions.assertEquals(a2Attendees, eventStatistics.getAttendees());
                Assertions.assertEquals(a2AttendeesWithDisq, eventStatistics.getAttendeesWithDisqualified());
            } else {
                throw new IllegalArgumentException(String.format("Unexpected category %s", eventStatistics.getCategory()));
            }
        });
    }

    private void verifyDisqualifiedEvent(Event event) {
        event.getResults().forEach(result -> {
            Assertions.assertEquals(-1, result.getTime().intValue());
            Assertions.assertEquals(-1, result.getPlace().intValue());
            Assertions.assertNull(result.getLoss());

            verifyEventStatistics(event, 0, 1, 0, 2);
        });
    }

    private OrisApiExtractionService orisApiExtractionServiceMock() {

        final OrisApiExtractionService service = Mockito.mock(OrisApiExtractionService.class);

        mockGetEventsCall(service);
        mockGetEventDetailCall(service);
        mockGetEventResultsCall(service);

        return service;
    }

    private void mockGetEventResultsCall(OrisApiExtractionService service) {
        List<ResultDTO> resultListNoDisq = validResultsAllFieldsFilled();
        Mockito.when(service.getEventResults(1L)).thenReturn(resultListNoDisq);

        List<ResultDTO> resultListDisq = resultsDisqualified();
        Mockito.when(service.getEventResults(2L)).thenReturn(resultListDisq);
    }

    //Different types of disqualified results
    private List<ResultDTO> resultsDisqualified() {
        final List<ResultDTO> results = new ArrayList<>();
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setClassDesc("A1");
        resultDTO.setLoss(null);
        resultDTO.setPlace("MS");
        resultDTO.setRegNo("G65A4SD");
        resultDTO.setTime("DISK");

        ResultDTO resultDTO1 = new ResultDTO();
        resultDTO1.setClassDesc("A2");
        resultDTO1.setLoss(null);
        resultDTO1.setPlace("DISK");
        resultDTO1.setRegNo("FWDF465");
        resultDTO1.setTime("VZDAL");

        ResultDTO resultDTO2 = new ResultDTO();
        resultDTO2.setClassDesc("A2");
        resultDTO2.setLoss(null);
        resultDTO2.setPlace(null);
        resultDTO2.setRegNo("DA6S54D");
        resultDTO2.setTime("DNS");

        results.add(resultDTO);
        results.add(resultDTO1);
        results.add(resultDTO2);

        return results;
    }

    //Different formats of valid results
    private List<ResultDTO> validResultsAllFieldsFilled() {
        final List<ResultDTO> results = new ArrayList<>();
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setClassDesc("A1");
        resultDTO.setLoss("+1:00");
        resultDTO.setPlace("2.");
        resultDTO.setRegNo("B3Q8D5A");
        resultDTO.setTime("2:12");
        results.add(resultDTO);

        ResultDTO resultDTO1 = new ResultDTO();
        resultDTO1.setClassDesc("A2");
        resultDTO1.setLoss(null);
        resultDTO1.setPlace("1");
        resultDTO1.setRegNo("T3Q8D5A");
        resultDTO1.setTime("1:12");
        results.add(resultDTO1);

        return results;
    }

    private void mockGetEventDetailCall(OrisApiExtractionService service) {
        final Event event = new Event();
        event.setDate(LocalDate.now());
        event.setName("Event1");
        event.setEventId(1L);
        Mockito.when(service.getEventDetail(1L)).thenReturn(event);

        final Event event1 = new Event();
        event1.setDate(LocalDate.now());
        event1.setName("Event2");
        event1.setEventId(2L);
        Mockito.when(service.getEventDetail(2L)).thenReturn(event1);
    }

    private void mockGetEventsCall(OrisApiExtractionService service) {
        final EventLite eventLite = new EventLite();
        eventLite.setEventId(1L);
        eventLite.setVersion(1);

        final EventLite eventLite1 = new EventLite();
        eventLite1.setEventId(2L);
        eventLite1.setVersion(2);

        Mockito.when(service.getEvents(Mockito.any(), Mockito.any())).thenReturn(List.of(eventLite, eventLite1));
    }
}