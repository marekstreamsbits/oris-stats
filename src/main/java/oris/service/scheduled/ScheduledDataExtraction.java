package oris.service.scheduled;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.utils.EventExtractionUtils;
import oris.service.OrisExtractionService;
import oris.service.events.EventsExtractionEvent;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Log4j2
@Service
public class ScheduledDataExtraction {

    private final OrisExtractionService orisExtractionService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public ScheduledDataExtraction(OrisExtractionService orisExtractionService, ApplicationEventPublisher applicationEventPublisher) {
        this.orisExtractionService = orisExtractionService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(cron = "${oris-stats.yesterdays.data.extraction.schedule}")
    public void extractYesterdaysData() {

        log.info("Starting job extract yesterday's data.");

        final LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
        final UUID jobId = UUID.randomUUID();

        final Collection<Event> events = orisExtractionService.extractAndPersistEventData(yesterday, yesterday);

        log.info("Finished job extract yesterday's data, added {} new events from day {}.", events.size(), yesterday);

        applicationEventPublisher.publishEvent(new EventsExtractionEvent(events, Collections.EMPTY_LIST, jobId,
                EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_DAILY));
        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_DAILY, jobId);

        applicationEventPublisher.publishEvent(new EventsExtractionEvent(Collections.EMPTY_LIST, EventExtractionUtils.addAttendees(events), jobId,
                EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_DAILY_FINISHED));
        log.info("Publishing event {} with jobID {}", EventsExtractionEvent.EventsExtractionJobType.EVENTS_EXTRACTED_DAILY_FINISHED, jobId);
    }
}