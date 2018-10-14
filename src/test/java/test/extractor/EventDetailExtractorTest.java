package test.extractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import oris.extractor.EventDetailOrisExtractor;

public class EventDetailExtractorTest {

    @Test
    public void missingEventIdTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EventDetailOrisExtractor().url());
    }

    @Test
    public void eventIdTest() {
        final EventDetailOrisExtractor eventDetailOrisExtractor = new EventDetailOrisExtractor();
        eventDetailOrisExtractor.withEventId(11L);
        Assertions.assertEquals("https://oris.orientacnisporty.cz/API/?format=json&method=getEvent&id=11", eventDetailOrisExtractor.url());
    }
}