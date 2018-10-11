package oris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import oris.model.db.*;
import oris.extractor.EventDetailOrisExtractor;
import oris.extractor.EventListOrisExtractor;
import oris.extractor.EventResultsOrisExtractor;
import oris.extractor.response.DataExtractor;
import oris.extractor.response.ResultDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OrisApiExtractionService {

    private final RestTemplate restTemplate;
    private final AttendeeService attendeeService;
    private final EventService eventService;
    private final ExecutorService executorService;

    private static final Logger LOG = LoggerFactory.getLogger(OrisApiExtractionService.class);

    public OrisApiExtractionService(RestTemplate restTemplate,
                                    AttendeeService attendeeService,
                                    @Qualifier("extractionServiceThreadpool") ExecutorService executorService,
                                    EventService eventService) {
        this.restTemplate = restTemplate;
        this.attendeeService = attendeeService;
        this.executorService = executorService;
        this.eventService = eventService;
    }

    private void saveEventInfo(Event eventDetail, Collection<ResultDTO> eventResults) {
        final Map<String, Attendee> attendeeMap = eventResults.parallelStream()
                .map(resultDTO -> resultDTO.getRegNo())
                .distinct()
                .collect(Collectors.toMap(regNo -> regNo, regNo -> attendeeService.findOrCreate(regNo)));
        List<Result> results = eventResults.parallelStream().map(resultDTO -> {
            Result result = new Result();
            result.setAttendee(attendeeMap.get(resultDTO.getRegNo()));
            result.setCategory(resultDTO.getClassDesc());
            result.setPlace(resolvePlace(resultDTO.getPlace()));
            result.setTime(resolveTime(resultDTO.getTime()));
            result.setLoss(resolveLoss(resultDTO.getLoss()));
            result.setEvent(eventDetail);
            return result;
        }).collect(Collectors.toList());
        eventDetail.setResults(results);
        eventDetail.setEventStatistics(resolveEventStatistics(eventDetail, results));
        eventService.save(eventDetail);
    }

    private Collection<EventStatistics> resolveEventStatistics(Event eventDetail, List<Result> results) {
        final Map<String, EventStatistics> categoryStatisticsMap = results
                .stream()
                .map(result -> result.getCategory())
                .distinct()
                .collect(Collectors.toMap(category -> category, category -> {
                    EventStatistics eventStatistics = new EventStatistics();
                    eventStatistics.setEvent(eventDetail);
                    eventStatistics.setCategory(category);
                    return eventStatistics;
                }));
        results.forEach(result -> {
                    final EventStatistics eventStatistics = categoryStatisticsMap.get(result.getCategory());
                    if (result.getPlace() == -1 || (result.getTime() != null && result.getTime() == -1)) {
                        eventStatistics.setAttendeesWithDisqualified(eventStatistics.getAttendeesWithDisqualified() + 1);
                    } else {
                        eventStatistics.setAttendees(eventStatistics.getAttendees() + 1);
                        eventStatistics.setAttendeesWithDisqualified(eventStatistics.getAttendeesWithDisqualified() + 1);
                    }
                    eventStatistics.setAttendees(eventStatistics.getAttendees());
                }
        );
        return categoryStatisticsMap.values();
    }

    //Loss comes in format "+ <min>:<sec>"; winner has this field null
    private Integer resolveLoss(String loss) {
        if (loss == null || loss.isEmpty()) {
            return null;
        }
        final String lossWithoutWhitespace = loss.replaceAll("\\s+", "");
        if (lossWithoutWhitespace.startsWith("+")) {
            return parseTimeFromMinSec(lossWithoutWhitespace.substring(1));
        } else if (lossWithoutWhitespace.isEmpty()) {
            return null;
        } else {
            LOG.warn("Unknown loss format '{}'.", lossWithoutWhitespace);
            return Integer.MIN_VALUE; //If this happens, we need to expect API responses for all possible cases.
        }
    }

    //Time comes in format "<min>:<sec>"
    private Integer resolveTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        if ("DISK".equals(time) || "VZDAL".equals(time) || "DNS".equals(time)) {
            return -1;
        }
        return parseTimeFromMinSec(time);
    }

    private Integer parseTimeFromMinSec(String time) {
        final String timeNoWhitespace = time.replaceAll("\\s+", "");
        if (timeNoWhitespace.isEmpty()) {
            return null;
        }
        String[] timeSegments = timeNoWhitespace.split(":");
        try {
            if (timeSegments.length == 1) {
                return Integer.parseInt(timeSegments[0]);
            } else {
                return Integer.parseInt(timeSegments[1]) + (Integer.parseInt(timeSegments[0]) * 60);
            }
        } catch (NumberFormatException ex) {
            LOG.error("Error while trying to resolve a time '{}' at an event.", timeNoWhitespace, ex);
            return Integer.MIN_VALUE;
        }
    }

    private Integer resolvePlace(String place) {
        if (place == null || place.isEmpty()) {
            return -1;
        }
        final String placeNoWhitespace = place.replaceAll("\\s+", "");

        //No idea what MS is, but it's there a lot. Probably the same as DISK.
        if ("MS".equals(placeNoWhitespace) || "DISK".equals(placeNoWhitespace)) {
            return -1;
        }

        try {
            //Places are written as "1." or "1"
            if (placeNoWhitespace.endsWith(".")) {
                return Integer.parseInt(placeNoWhitespace.substring(0, placeNoWhitespace.length() - 1));
            } else {
                return Integer.parseInt(placeNoWhitespace);
            }
        } catch (NumberFormatException ex) {
            LOG.error("Error while trying to resolve a place '{}' at an event.", placeNoWhitespace, ex);
            return Integer.MIN_VALUE;
        }
    }

    public void extractAndPersistEventData(LocalDate fromDay, LocalDate toDay) {
        final Collection<EventLite> events = getEvents(fromDay, toDay);
        ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);
        events.forEach(event ->
                completionService.submit(() -> {

                    final Event eventDetail = OrisApiExtractionService.this.getEventDetail(event.getEventId());
                    final Collection<ResultDTO> eventResults = OrisApiExtractionService.this.getEventResults(event.getEventId());
                    OrisApiExtractionService.this.saveEventInfo(eventDetail, eventResults);

                    return null;
                })
        );
        int eventCount = events.size();
        for (int i = 0; i < eventCount; i++) {
            try {
                final Future completedFutureTask = completionService.take();
                completedFutureTask.get();
            } catch (InterruptedException e) {
                LOG.error("Unexpected exception while processing data from ORIS.", e);
            } catch (ExecutionException e) {
                LOG.error("Error while processing data from ORIS.", e);
            }
        }
    }

    /**
     * For OB only. All events.
     * Id & Version info only!
     *
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public Collection<EventLite> getEvents(LocalDate dateFrom, LocalDate dateTo) {
        EventListOrisExtractor eventListOrisExtractor = new EventListOrisExtractor();
        eventListOrisExtractor.withFromDate(dateFrom);
        eventListOrisExtractor.withToDate(dateTo);

        DataExtractor<Collection<EventLite>> dataExtractor = restTemplate.getForObject(eventListOrisExtractor.url(), eventListOrisExtractor.dataExtractor());

        return dataExtractor.getData();
    }

    public Event getEventDetail(Long eventId) {
        EventDetailOrisExtractor detailOrisExtractor = new EventDetailOrisExtractor();
        detailOrisExtractor.withEventId(eventId);

        DataExtractor<Event> eventDataExtractor = restTemplate.getForObject(detailOrisExtractor.url(), detailOrisExtractor.dataExtractor());

        return eventDataExtractor.getData();
    }

    public Collection<ResultDTO> getEventResults(Long eventId) {
        EventResultsOrisExtractor eventResultsExtractor = new EventResultsOrisExtractor();
        eventResultsExtractor.withEventId(eventId);

        DataExtractor<Collection<ResultDTO>> resultsExtractor = restTemplate.getForObject(eventResultsExtractor.url(), eventResultsExtractor.dataExtractor());

        return resultsExtractor.getData();
    }
}