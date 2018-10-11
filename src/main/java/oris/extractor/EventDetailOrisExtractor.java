package oris.extractor;

import oris.model.db.Event;
import oris.extractor.response.DataExtractor;
import oris.extractor.response.EventResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventDetailOrisExtractor extends BaseOrisExtractor<Event> {

    private Long eventId;

    @Override
    protected String method() {
        return "getEvent";
    }

    @Override
    protected Collection<String> params() {
        if (eventId == null) {
            throw new IllegalArgumentException("id cannot be null for getEvent method");
        }
        List<String> params = new ArrayList<>();
        params.add("id=" + eventId);
        return params;
    }

    public void withEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public Class<? extends DataExtractor<Event>> dataExtractor() {
        return EventResponse.class;
    }
}