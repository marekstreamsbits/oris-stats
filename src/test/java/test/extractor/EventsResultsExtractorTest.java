package test.extractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import oris.extractor.EventResultsOrisExtractor;

public class EventsResultsExtractorTest {

    @Test
    public void missingEventIdTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EventResultsOrisExtractor().url());
    }

    @Test
    public void eventIdTest() {
        final EventResultsOrisExtractor eventResultsOrisExtractor = new EventResultsOrisExtractor();
        eventResultsOrisExtractor.withEventId(11L);
        Assertions.assertEquals("https://oris.orientacnisporty.cz/API/?format=json&method=getEventResults&eventid=11", eventResultsOrisExtractor.url());
    }
}