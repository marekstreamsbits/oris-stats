package oris.service.events;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEvent;
import oris.model.db.Event;

import java.util.Collection;
import java.util.UUID;

@Data
@Log4j2
public class EventsExtractionEvent extends ApplicationEvent {

    private final UUID jobId;
    private final EventsExtractionJobType eventsExtractionJobType;
    private final Collection<Event> events;
    private final Collection<Long> attendeeIds;

    public EventsExtractionEvent(final Collection<Event> events, final Collection<Long> attendeeIds, final UUID jobId, EventsExtractionJobType eventsExtractionJobType) {
        super(events);
        this.jobId = jobId;
        this.eventsExtractionJobType = eventsExtractionJobType;
        this.events = events;
        this.attendeeIds = attendeeIds;
    }

    public enum EventsExtractionJobType {
        EVENTS_EXTRACTED_INITIAL,
        EVENTS_EXTRACTION_INITIAL_FINISHED,
        EVENTS_EXTRACTED_DAILY,
        EVENTS_EXTRACTED_DAILY_FINISHED;
    }
}