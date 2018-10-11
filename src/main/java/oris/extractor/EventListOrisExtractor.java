package oris.extractor;

import oris.model.db.Event;
import oris.model.db.EventLite;
import oris.extractor.response.DataExtractor;
import oris.extractor.response.EventListVersionsResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EventListOrisExtractor extends BaseOrisExtractor<Collection<EventLite>> {

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Event.EventType eventType = Event.EventType.OB;

    private LocalDate fromDate;
    private LocalDate toDate;

    @Override
    protected String method() {
        return "getEventListVersions";
    }

    @Override
    protected Collection<String> params() {
        List<String> params = new ArrayList<>();
        params.add("sport=" + eventType.getCode());
        addIfNotNull("datefrom", fromDate == null ? null : DATE_FORMAT.format(fromDate), params);
        addIfNotNull("dateto", toDate == null ? null : DATE_FORMAT.format(toDate), params);
        params.add("all=" + 1); // Event outside of official calendar as well.
        return params;
    }

    public void withEventType(Event.EventType eventType) {
        this.eventType = eventType;
    }

    public void withFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public void withToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public Class<? extends DataExtractor<Collection<EventLite>>> dataExtractor() {
        return EventListVersionsResponse.class;
    }
}