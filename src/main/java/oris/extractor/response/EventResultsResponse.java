package oris.extractor.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventResultsResponse implements DataExtractor<Collection<ResultDTO>> {

    @JsonDeserialize(using = ResultDTODeserializer.class)
    private Map<String, ResultDTO> data = new HashMap<>();

    @Override
    public Collection<ResultDTO> getData() {
        return data.values();
    }
}