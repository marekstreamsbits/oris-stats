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

        log.info("Starting loading data from ORIS. From date " + startDate);

        //TODO check for already populated database - check latest added event - start from there in case

        final Set<Long> attendeeIds = new HashSet<>();
        final UUID jobId = UUID.randomUUID();
        final LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
        LocalDate fromDay = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate toDay = toDay(yesterday, fromDay, maxDaysAtOnce);

        log.info("Dropping indexes on event_rivalries table.");
        eventRivalryRepository.dropIndexes();

        while (!fromDay.equals(toDay)) {
            final Collection<Event> events = extractAndPersistEventData(jobId, fromDay, toDay);
            attendeeIds.addAll(EventExtractionUtils.addAttendees(events));
            fromDay = LocalDate.from(toDay).plus(1, ChronoUnit.DAYS); //API is inclusive in this filter
            toDay = toDay(yesterday, fromDay, maxDaysAtOnce);
        }

        log.info("Finished loading data from ORIS.");

        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTION_INITIAL_FINISHED, jobId);
        applicationEventPublisher.publishEvent(
                new EventsExtractionEvent(Collections.EMPTY_LIST, attendeeIds, jobId, String.format("EVENTS_EXTRACTION_INITIAL_FINISHED %s", LocalDate.now().toString()),
                        EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTION_INITIAL_FINISHED));
    }

    private Collection<Event> extractAndPersistEventData(UUID jobId, LocalDate fromDay, LocalDate toDay) {
        log.info("Extracting data from " + fromDay + " to " + toDay);
        final Collection<Event> events = orisExtractionService.extractAndPersistEventData(fromDay, toDay);
        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_INITIAL, jobId);
        applicationEventPublisher.publishEvent(
                new EventsExtractionEvent(events, Collections.EMPTY_LIST, jobId, String.format("EVENTS_EXTRACTED_INITIAL from %s to %s", fromDay.toString(), toDay.toString()),
                        EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_INITIAL));
        return events;
    }

    private LocalDate toDay(LocalDate yesterday, LocalDate fromDay, int maxDaysAtOnce) {
        int daysAdded = 0;
        LocalDate toDate = LocalDate.from(fromDay);

        while (daysAdded != maxDaysAtOnce && toDate.isBefore(yesterday)) {
            toDate = toDate.plus(1, ChronoUnit.DAYS);
            daysAdded++;
        }
        return toDate;
    }
}