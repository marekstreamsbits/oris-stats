package oris.service.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import oris.model.db.Event;
import oris.service.RivalryComputationService;
import oris.service.events.EventsExtractionEvent;
import oris.utils.ThreadingUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Log4j2
@Component
public class RivalryComputationListener implements ApplicationListener<EventsExtractionEvent> {

    private static final Map<UUID, Semaphore> jobSemaphors = new ConcurrentHashMap<>();

    private final RivalryComputationService rivalryComputationService;

    private static final int MAX_SEMAPHOR_PERMITS = ThreadingUtils.defaultThreadCount(); // Maximum running jobs at one time. Ensures global rivalries are counted after all jobs of ID finished.

    public RivalryComputationListener(RivalryComputationService rivalryComputationService) {
        this.rivalryComputationService = rivalryComputationService;
    }

    @Override
    public void onApplicationEvent(EventsExtractionEvent event) {

        log.info("Requested to process event: {} with jobID: {} and name {}", event.getEventsExtractionJobType(), event.getJobId(), event.getName());

        final Semaphore semaphore = jobSemaphors.computeIfAbsent(event.getJobId(), uuid -> new Semaphore(MAX_SEMAPHOR_PERMITS));

        switch (event.getEventsExtractionJobType()) {
            case EVENTS_EXTRACTED_INITIAL:
                handleEventRivalriesComputation(event, semaphore);
                break;

            case EVENTS_EXTRACTION_INITIAL_FINISHED:
                handleGlobalRivalriesInitialComputation(event, semaphore);
                break;

            case EVENTS_EXTRACTED_DAILY:
                handleEventRivalriesComputation(event, semaphore);
                break;

            case EVENTS_EXTRACTED_DAILY_FINISHED:
                handleGlobalRivalriesDailyComputation(event, semaphore);
                break;
        }
        log.info("Processed event: {} with jobID: {} and name {}", event.getEventsExtractionJobType(), event.getJobId(), event.getName());
    }

    private void handleEventRivalriesComputation(EventsExtractionEvent event, Semaphore semaphore) {
        boolean acquired = false;
        try {
            semaphore.acquire();
            acquired = true;
            log.info("Started to process event: {} with jobID: {} and name {}", event.getEventsExtractionJobType(), event.getJobId(), event.getName());
            rivalryComputationService.computeAndPersistRivalries(event.getEvents());
        } catch (InterruptedException e) {
            log.error("Could not acquire a job slot from the semaphor.", e);
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    private void handleGlobalRivalriesInitialComputation(EventsExtractionEvent event, Semaphore semaphore) {
        try {
            semaphore.acquire(MAX_SEMAPHOR_PERMITS);
            log.info("Started to process event: {} with jobID: {} and name {}", event.getEventsExtractionJobType(), event.getJobId(), event.getName());
            rivalryComputationService.computeGlobalRivalriesInitial(event.getAttendeeIds());
        } catch (InterruptedException e) {
            log.error("Could not acquire a job slot from the semaphor.", e);
        } finally {
            jobSemaphors.remove(event.getJobId());
        }
    }

    private void handleGlobalRivalriesDailyComputation(EventsExtractionEvent event, Semaphore semaphore) {
        try {
            semaphore.acquire(MAX_SEMAPHOR_PERMITS);
            log.info("Started to process event: {} with jobID: {} and name {}", event.getEventsExtractionJobType(), event.getJobId(), event.getName());
            rivalryComputationService.computeGlobalRivalriesDaily(event.getAttendeeIds(), event.getEvents().stream().map(Event::getId).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            log.error("Could not acquire a job slot from the semaphor.", e);
        } finally {
            jobSemaphors.remove(event.getJobId());
        }
    }
}