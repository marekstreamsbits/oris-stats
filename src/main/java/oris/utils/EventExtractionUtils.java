package oris.utils;

import oris.model.db.Event;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EventExtractionUtils {

    private EventExtractionUtils() {
    }

    public static Set<Long> addAttendees(final Collection<Event> events) {
        final Set<Long> attendeeIds = new HashSet<>();
        events.forEach(
                event -> event.getResults().forEach(
                        result -> attendeeIds.add(result.getAttendee().getId())
                )
        );
        return attendeeIds;
    }
}