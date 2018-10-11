package oris.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import oris.service.OrisApiExtractionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class StartupDataExtraction {

    private final OrisApiExtractionService orisExtractionService;
    private final String startDate;
    private final int maxDaysAtOnce;
    private final boolean populateDbOnStartup;

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Logger LOG = LoggerFactory.getLogger(StartupDataExtraction.class);

    @Autowired
    public StartupDataExtraction(OrisApiExtractionService orisExtractionService,
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
            LOG.info("Not running initial data load from ORIS. Change 'oris-stats.download.data.from.oris.on.startup' if you want to load data from ORIS.");
            return;
        }

        LOG.info("Starting loading data from ORIS. From date " + startDate);

        //TODO check for already populated database - check latest added event - start from there in case

        LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
        LocalDate fromDay = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate toDay = toDay(yesterday, fromDay, maxDaysAtOnce);

        while (!fromDay.equals(toDay)) {
            LOG.info("Extracting data from " + fromDay + " to " + toDay);
            orisExtractionService.extractAndPersistEventData(fromDay, toDay);
            fromDay = LocalDate.from(toDay).plus(1, ChronoUnit.DAYS); //API is inclusive in this filter
            toDay = toDay(yesterday, fromDay, maxDaysAtOnce);
        }

        LOG.info("Finished loading data from ORIS.");
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