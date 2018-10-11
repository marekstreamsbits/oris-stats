package oris.extractor;

import oris.extractor.response.DataExtractor;
import oris.extractor.response.EventResultsResponse;
import oris.extractor.response.ResultDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventResultsOrisExtractor extends BaseOrisExtractor<Collection<ResultDTO>> {

    private Long eventId;

    @Override
    protected String method() {
        return "getEventResults";
    }

    @Override
    protected Collection<String> params() {
        if (eventId == null) {
            throw new IllegalArgumentException("EventId parameter has to be present for the getEventResults method!");
        }
        List<String> params = new ArrayList<>();
        params.add("eventid=" + eventId);
        return params;
    }

    public void withEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public Class<? extends DataExtractor<Collection<ResultDTO>>> dataExtractor() {
        return EventResultsResponse.class;
    }
}