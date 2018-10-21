package oris.scheduler;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import oris.service.OrisExtractionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Log4j2
@Service
public class StartupDataExtraction {

    private final OrisExtractionService orisExtractionService;
    private final String startDate;
    private final int maxDaysAtOnce;
    private final boolean populateDbOnStartup;

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public StartupDataExtraction(OrisExtractionService orisExtractionService,
                                 @Value("${oris-stats.start.date}") String startDate,
                                 @Value("${oris-stats.max.days.extraction.at.once}") int maxDaysAtOnce,
                                 @Value("${oris-stats.download.data.from.oris.on.startup}") Boolean populateDbOnStartup) {
        this.orisExtractionService = orisExtractionService;
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

        LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
        LocalDate fromDay = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate toDay = toDay(yesterday, fromDay, maxDaysAtOnce);

        while (!fromDay.equals(toDay)) {
            log.info("Extracting data from " + fromDay + " to " + toDay);
            orisExtractionService.extractAndPersistEventData(fromDay, toDay);
            fromDay = LocalDate.from(toDay).plus(1, ChronoUnit.DAYS); //API is inclusive in this filter
            toDay = toDay(yesterday, fromDay, maxDaysAtOnce);
        }

        log.info("Finished loading data from ORIS.");
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