package test.extractor.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import oris.Application;
import oris.extractor.response.EventResultsResponse;

import java.io.File;
import java.io.IOException;

//Testing custom deserializers
public class DeserializerTest {

    private static final ObjectMapper OBJECT_MAPPER = new Application().orisObjectMapper(); // ObjectMapper used only in RestTemplate where objects are extracted, not app wide.

    @Test
    public void emptyMapResultsDeserializerTest() throws IOException {
        EventResultsResponse eventResultsResponse = OBJECT_MAPPER.readValue(getResourceFile("event_results/eventResults_empty.json"), EventResultsResponse.class);
        Assertions.assertTrue(eventResultsResponse.getData().isEmpty());
    }

    @Test
    public void nonEmptyResultsDeserializerTest() throws IOException {
        EventResultsResponse eventResultsResponse = OBJECT_MAPPER.readValue(getResourceFile("event_results/eventResults_full.json"), EventResultsResponse.class);
        Assertions.assertTrue(eventResultsResponse.getData().size() > 0);
    }

    private File getResourceFile(String pathToFile) {
        return new File(this.getClass().getClassLoader().getResource(pathToFile).getFile());
    }
}