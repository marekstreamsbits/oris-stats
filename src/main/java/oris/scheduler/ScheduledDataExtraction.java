package oris.scheduler;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.service.OrisExtractionService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Log4j2
@Service
public class ScheduledDataExtraction {

    private final OrisExtractionService orisExtractionService;

    public ScheduledDataExtraction(OrisExtractionService orisExtractionService) {
        this.orisExtractionService = orisExtractionService;
    }

    @Scheduled(cron = "${oris-stats.yesterdays.data.extraction.schedule}")
    public void extractYesterdaysData() {

        log.info("Starting job extract yesterday's data.");

        final LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);

        final Collection<Event> events = orisExtractionService.extractAndPersistEventData(yesterday, yesterday);

        log.info("Finished job extract yesterday's data, added {} new events from day {}.", events.size(), yesterday);
    }
}