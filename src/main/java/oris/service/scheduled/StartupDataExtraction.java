package oris.service.scheduled;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.repository.EventRivalryRepository;
import oris.service.OrisExtractionService;
import oris.service.events.EventsExtractionEvent;
import oris.utils.EventExtractionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
public class StartupDataExtraction {

    private final OrisExtractionService orisExtractionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final EventRivalryRepository eventRivalryRepository;

    private final String startDate;
    private final int maxDaysAtOnce;
    private final boolean populateDbOnStartup;

    private static volatile boolean startupExtractionInProgress = false;

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public StartupDataExtraction(OrisExtractionService orisExtractionService,
                                 ApplicationEventPublisher applicationEventPublisher,
                                 EventRivalryRepository eventRivalryRepository,
                                 @Value("${oris-stats.start.date}") String startDate,
                                 @Value("${oris-stats.max.days.extraction.at.once}") int maxDaysAtOnce,
                                 @Value("${oris-stats.download.data.from.oris.on.startup}") Boolean populateDbOnStartup) {
        this.orisExtractionService = orisExtractionService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventRivalryRepository = eventRivalryRepository;
        this.startDate = startDate;
        this.maxDaysAtOnce = maxDaysAtOnce;
        this.populateDbOnStartup = populateDbOnStartup;
    }

    /**
     * Gets data from @Value("${oris-stats.start.date}") until yesterday.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void populateDatabaseAfterStartup() {

        if (!populateDbOnStartup) {
            log.info("Not running initial data load from ORIS. Change 'oris-stats.download.data.from.oris.on.startup' if you want to load data from ORIS.");
            return;
        }

        startupExtractionInProgress = true;

        log.info("Starting loading data from ORIS. From date " + startDate);

        final Set<Long> attendeeIds = new HashSet<>();
        final UUID jobId = UUID.randomUUID();
        final LocalDate today = LocalDate.now();
        LocalDate fromDay = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate toDay = toDay(today, fromDay, maxDaysAtOnce);

        log.info("Dropping indexes on event_rivalries table.");
        eventRivalryRepository.dropIndexes();

        while (!fromDay.equals(toDay)) {
            final Collection<Event> events = extractAndPersistEventData(jobId, fromDay, toDay);
            attendeeIds.addAll(EventExtractionUtils.extractAttendeeIds(events));
            fromDay = LocalDate.from(toDay).plus(1, ChronoUnit.DAYS); //API is inclusive in this filter
            toDay = toDay(today, fromDay, maxDaysAtOnce);
        }

        log.info("Finished loading data from ORIS.");

        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTION_INITIAL_FINISHED, jobId);
        applicationEventPublisher.publishEvent(
                new EventsExtractionEvent(Collections.EMPTY_LIST, attendeeIds, jobId, String.format("EVENT_RESULTS_EXTRACTION_INITIAL_FINISHED %s", LocalDate.now().toString()),
                        EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTION_INITIAL_FINISHED));
    }

    // TODO Detect this ongoing event differently and not through this flag.
    public static boolean startupExtractionAndComputationInProgress() {
        return startupExtractionInProgress;
    }

    // TODO Detect this ongoing event differently and not through this flag.
    public static void finishedStartupExtrationAndComputation() {
        startupExtractionInProgress = false;
    }

    private Collection<Event> extractAndPersistEventData(UUID jobId, LocalDate fromDay, LocalDate toDay) {
        log.info("Extracting data from " + fromDay + " to " + toDay);
        final Collection<Event> events = orisExtractionService.extractAndPersistEventDataWithResults(fromDay, toDay);
        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_INITIAL, jobId);
        applicationEventPublisher.publishEvent(
                new EventsExtractionEvent(events, Collections.EMPTY_LIST, jobId, String.format("EVENT_RESULTS_EXTRACTED_INITIAL from %s to %s", fromDay.toString(), toDay.toString()),
                        EventsExtractionEvent.EventsExtractionJobType.EVENT_RESULTS_EXTRACTED_INITIAL));
        return events;
    }

    /**
     * Calculates the day to which (included) we are extracting statistics.
     *
     * @param today
     * @param fromDay
     * @param maxDaysAtOnce
     * @return
     */
    private LocalDate toDay(final LocalDate today, final LocalDate fromDay, final int maxDaysAtOnce) {
        int daysAdded = 0;
        LocalDate toDate = LocalDate.from(fromDay);

        while (daysAdded != maxDaysAtOnce && toDate.isBefore(today)) {
            toDate = toDate.plus(1, ChronoUnit.DAYS);
            daysAdded++;
        }
        return toDate;
    }
}