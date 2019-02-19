package oris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import oris.extractor.response.ResultDTO;
import oris.model.db.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class OrisExtractionService {

    private final AttendeeService attendeeService;
    private final EventService eventService;
    private final ExecutorService executorService;
    private final OrisApiExtractionService orisApiExtractionService;

    private static final Logger LOG = LoggerFactory.getLogger(OrisExtractionService.class);

    public OrisExtractionService(AttendeeService attendeeService,
                                 @Qualifier("extractionServiceThreadpool") ExecutorService executorService,
                                 EventService eventService, OrisApiExtractionService orisApiExtractionService) {
        this.orisApiExtractionService = orisApiExtractionService;
        this.attendeeService = attendeeService;
        this.executorService = executorService;
        this.eventService = eventService;
    }

    /**
     * Trying to extract and persist all data about an event, including the results.
     *
     * @param fromDay
     * @param toDay
     * @return
     */
    public Collection<Event> extractAndPersistEventDataWithResults(final LocalDate fromDay, final LocalDate toDay) {
        final Collection<EventLite> events = orisApiExtractionService.getEvents(fromDay, toDay);
        final ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);
        events.forEach(event ->
                completionService.submit(() -> {
                    final Event eventDetail = orisApiExtractionService.getEventDetail(event.getEventId());
                    final Collection<ResultDTO> eventResults = orisApiExtractionService.getEventResults(event.getEventId());
                    return saveEventInfo(eventDetail, eventResults);
                })
        );
        return finishEventTasks(events, completionService);
    }

    public Collection<Event> extractAndPersistTodaysEventData() {
        final LocalDate toDay = LocalDate.now();
        final Collection<EventLite> events = orisApiExtractionService.getEvents(toDay, toDay);

        final ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);
        events.forEach(event ->
                completionService.submit(() -> {
                    final Event eventDetail = orisApiExtractionService.getEventDetail(event.getEventId());
                    return saveEventInfo(eventDetail);
                })
        );
        return finishEventTasks(events, completionService);
    }

    public Collection<Event> extractAndPersistEventResultsData(final Collection<Event> todaysEventsWithoutResults) {
        final ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);
        final List<Event> eventsWithNewlyAddedResults = new ArrayList<>();
        todaysEventsWithoutResults.forEach(event ->
                completionService.submit(() -> {
                    final Event eventDetail = orisApiExtractionService.getEventDetail(event.getEventId());
                    final Collection<ResultDTO> eventResults = orisApiExtractionService.getEventResults(event.getEventId());
                    if (eventResults.isEmpty()) {
                        return eventDetail;
                    }
                    final Event eventWithSavedResults = saveEventInfo(eventDetail, eventResults);
                    ;
                    eventsWithNewlyAddedResults.add(eventWithSavedResults);
                    return eventWithSavedResults;
                })
        );
        finishEventTasks((new ArrayList<>(todaysEventsWithoutResults)), completionService);
        return eventsWithNewlyAddedResults;
    }

    protected Collection<Event> finishEventTasks(Collection<EventLite> events, ExecutorCompletionService completionService) {
        final List<Event> persistedEvents = new ArrayList<>();
        int eventCount = events.size();
        for (int i = 0; i < eventCount; i++) {
            try {
                final Future<Event> completedFutureTask = completionService.take();
                persistedEvents.add(completedFutureTask.get());
            } catch (InterruptedException e) {
                LOG.error("Unexpected exception while processing data from ORIS.", e);
            } catch (ExecutionException e) {
                LOG.error("Error while processing data from ORIS.", e);
            }
        }
        return persistedEvents;
    }

    private Event saveEventInfo(final Event eventDetail) {
        return eventService.save(eventDetail);
    }

    private Event saveEventInfo(Event eventDetail, Collection<ResultDTO> eventResults) {
        final Map<String, Attendee> attendeeMap = eventResults.stream()
                .map(ResultDTO::getRegNo)
                .distinct()
                .collect(Collectors.toMap(regNo -> regNo, attendeeService::findOrCreate));
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
        return eventService.save(eventDetail);
    }

    private Collection<EventStatistics> resolveEventStatistics(final Event eventDetail, final List<Result> results) {
        final Map<String, EventStatistics> categoryStatisticsMap = results
                .stream()
                .map(Result::getCategory)
                .distinct()
                .collect(Collectors.toMap(category -> category, category -> {
                    EventStatistics eventStatistics = new EventStatistics();
                    eventStatistics.setEvent(eventDetail);
                    eventStatistics.setCategory(category);
                    return eventStatistics;
                }));
        results.forEach(result -> calcualteEventStatistics(categoryStatisticsMap, result));
        return categoryStatisticsMap.values();
    }

    private void calcualteEventStatistics(Map<String, EventStatistics> categoryStatisticsMap, Result result) {
        final EventStatistics eventStatistics = categoryStatisticsMap.get(result.getCategory());
        if (result.getPlace() == -1 || (result.getTime() != null && result.getTime() == -1)) {
            eventStatistics.setAttendeesWithDisqualified(eventStatistics.getAttendeesWithDisqualified() + 1);
        } else {
            eventStatistics.setAttendees(eventStatistics.getAttendees() + 1);
            eventStatistics.setAttendeesWithDisqualified(eventStatistics.getAttendeesWithDisqualified() + 1);
        }
        eventStatistics.setAttendees(eventStatistics.getAttendees());
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

    /**
     * @param time
     * @return time in sec.; -1 if disqualified
     */
    private Integer resolveTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        if ("DISK".equals(time) || "VZDAL".equals(time) || "DNS".equals(time)) {
            return -1;
        }
        return parseTimeFromMinSec(time);
    }

    //Time comes in format "<min>:<sec>"
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

    /**
     * @param place
     * @return place as int >= 1; -1 if disqualified
     */
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
}