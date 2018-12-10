package oris.service.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import oris.service.RivalryComputationService;
import oris.service.events.EventsExtractionEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Log4j2
@Component
public class RivalryComputationListener implements ApplicationListener<EventsExtractionEvent> {

    private static final Map<UUID, Semaphore> jobSemaphors = new ConcurrentHashMap<>();

    private final RivalryComputationService rivalryComputationService;

    private static final int MAX_SEMAPHOR_PERMITS = 10; // Maximum 10 running jobs at one time. Ensures global rivalries are counted after all jobs are finished.

    public RivalryComputationListener(RivalryComputationService rivalryComputationService) {
        this.rivalryComputationService = rivalryComputationService;
    }

    @Override
    public void onApplicationEvent(EventsExtractionEvent event) {

        log.info("Requested to process event: {} with jobID: {}", event.getEventsExtractionJobType(), event.getJobId());

        final Semaphore semaphore = jobSemaphors.computeIfAbsent(event.getJobId(), uuid -> new Semaphore(MAX_SEMAPHOR_PERMITS));

        switch (event.getEventsExtractionJobType()) {
            case EVENTS_EXTRACTED_INITIAL:
                handleEventRivalriesCompuation(event, semaphore);
                break;

            case EVENTS_EXTRACTION_INITIAL_FINISHED:
                handleGlobalRivalriesComputation(event, semaphore);
                break;

            case EVENTS_EXTRACTED_DAILY:
                // TODO Marek implement
                break;

            case EVENTS_EXTRACTED_DAILY_FINISHED:
                // TODO Marek implement
                break;
        }

        log.info("Processed event: {} with jobID: {}", event.getEventsExtractionJobType(), event.getJobId());
    }

    private void handleEventRivalriesCompuation(EventsExtractionEvent event, Semaphore semaphore) {
        boolean acquired = false;
        try {
            semaphore.acquire();
            acquired = true;
            log.info("Started to process event: {} with jobID: {}", event.getEventsExtractionJobType(), event.getJobId());
            rivalryComputationService.computeAndPersistRivalries(event.getEvents());
        } catch (InterruptedException e) {
            log.error("Could not acquire a job slot from the semaphor.", e);
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    private void handleGlobalRivalriesComputation(EventsExtractionEvent event, Semaphore semaphore) {
        try {
            semaphore.acquire(MAX_SEMAPHOR_PERMITS);
            log.info("Started to process event: {} with jobID: {}", event.getEventsExtractionJobType(), event.getJobId());
            rivalryComputationService.computeGlobalRivalries(event.getAttendeeIds());
        } catch (InterruptedException e) {
            log.error("Could not acquire a job slot from the semaphor.", e);
        } finally {
            jobSemaphors.remove(event.getJobId());
        }
    }
}