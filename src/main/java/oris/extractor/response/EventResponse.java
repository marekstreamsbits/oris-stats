package oris.extractor.response;

import oris.model.db.Event;

public class EventResponse implements DataExtractor<Event> {

    private Event data;

    @Override
    public Event getData() {
        return data;
    }
}