package oris.extractor.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import oris.model.db.EventLite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventListVersionsResponse implements DataExtractor<Collection<EventLite>> {

    @JsonDeserialize(using = EventLiteMapDeserializer.class)
    private Map<String, EventLite> data = new HashMap<>();

    @Override
    public Collection<EventLite> getData() {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }
        return data.values();
    }
}