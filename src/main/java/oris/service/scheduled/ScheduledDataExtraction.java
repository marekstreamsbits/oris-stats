package oris.service.scheduled;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.service.EventService;
import oris.service.OrisExtractionService;
import oris.service.events.EventsExtractionEvent;
import oris.utils.EventExtractionUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Log4j2
@Service
public class ScheduledDataExtraction {

    private final OrisExtractionService orisExtractionService;
    private final EventService eventService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public ScheduledDataExtraction(OrisExtractionService orisExtractionService, EventService eventService, ApplicationEventPublisher applicationEventPublisher) {
        this.orisExtractionService = orisExtractionService;
        this.eventService = eventService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(cron = "${oris-stats.todays.data.extraction.schedule}")
    public void extractTodaysEventData() {
        if (StartupDataExtraction.startupExtractionAndComputationInProgress()) {
            return;
        }

        log.info("Starting job extract today's event data.");

        final LocalDate today = LocalDate.now();
        final Collection<Event> events = orisExtractionService.extractAndPersistTodaysEventData();

        log.info("Finished job extract today's event data, added {} new events from day {}.", events.size(), today);
    }

    @Scheduled(cron = "${oris-stats.todays.results.extraction.schedule}")
    public void extractTodaysEventResultsData() {
        if (StartupDataExtraction.startupExtractionAndComputationInProgress()) {
            return;
        }
        log.info("Starting job extract today's event results  data.");

        final Collection<Event> todaysEventsWithoutResults = eventService.getTodaysEventsWithoutResults();
        final Collection<Event> todaysEventsWithFreshlyAddedResults = orisExtractionService.extractAndPersistEventResultsData(todaysEventsWithoutResults);

        log.info("Finished job extract today's event results data. Now sending {} events rivalries computation.", todaysEventsWithFreshlyAddedResults.size());

        sendNewlyAddedResultsForRivalryComputation(todaysEventsWithFreshlyAddedResults);
    }

    /**
     * Looks at the events from the past month that don't have results and tries to get them.
     */
    @Scheduled(cron = "${oris-stats.past.results.extraction.schedule}")
    public void extractPastEventResultsData() {
        if (StartupDataExtraction.startupExtractionAndComputationInProgress()) {
            return;
        }
        log.info("Starting job extract today's event results  data.");

        final Collection<Event> pastMonthEventsWithoutResults = eventService.getPastMonthEventsWithoutResults();
        final Collection<Event> eventsWithFreshlyAddedResults = orisExtractionService.extractAndPersistEventResultsData(pastMonthEventsWithoutResults);

        log.info("Finished job extract p[ast month's event results data. Now sending {} events rivalries computation.", eventsWithFreshlyAddedResults.size());

        sendNewlyAddedResultsForRivalryComputation(eventsWithFreshlyAddedResults);
    }

    protected void sendNewlyAddedResultsForRivalryComputation(final Collection<Event> eventsWithFreshlyAddedResults) {
        if (!eventsWithFreshlyAddedResults.isEmpty()) {
            final LocalDate today = LocalDate.now();
            final UUID jobId = UUID.randomUUID();

            applicationEventPublisher.publishEvent(new EventsExtractionEvent(eventsWithFreshlyAddedResults, Collections.EMPTY_LIST, jobId,
                    String.format("EVENT_RESULTS_EXTRACTED_DAILY from %s to %s", today.toString(), today.toString()), EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_DAILY));
            log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_DAILY, jobId);

            final Collection<Long> attendeeIds = EventExtractionUtils.extractAttendeeIds(eventsWithFreshlyAddedResults);

            applicationEventPublisher.publishEvent(new EventsExtractionEvent(Collections.EMPTY_LIST, attendeeIds, jobId,
                    String.format("DAILY_GLOBAL_RIVALRIES from %s of %d events and %d attendees", today.toString(), eventsWithFreshlyAddedResults.size(), attendeeIds.size()), EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_DAILY_FINISHED));
            log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_DAILY_FINISHED, jobId);
        }
    }
}