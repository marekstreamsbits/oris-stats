package test.extractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import oris.extractor.EventListOrisExtractor;
import oris.model.db.Event;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EventListExtractorTest {

    @Test
    public void listWithoutDates() {
        final EventListOrisExtractor eventListOrisExtractor = new EventListOrisExtractor();
        eventListOrisExtractor.withEventType(Event.EventType.OB);
        Assertions.assertEquals("https://oris.orientacnisporty.cz/API/?format=json&method=getEventListVersions&sport=1&all=1", eventListOrisExtractor.url());
    }

    @Test
    public void listWithDates() {
        final EventListOrisExtractor eventListOrisExtractor = new EventListOrisExtractor();
        final LocalDate date = LocalDate.of(2018, 10, 20);
        eventListOrisExtractor.withFromDate(date);
        eventListOrisExtractor.withToDate(date.plus(1, ChronoUnit.DAYS));
        Assertions.assertEquals("https://oris.orientacnisporty.cz/API/?format=json&method=getEventListVersions&sport=1&datefrom=2018-10-20&dateto=2018-10-21&all=1", eventListOrisExtractor.url());
    }
}