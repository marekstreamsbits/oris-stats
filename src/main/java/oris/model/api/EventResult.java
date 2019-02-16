package oris.model.api;

import lombok.Data;

import java.time.LocalDate;
import java.util.Comparator;

@Data
public class EventResult {

    public static final Comparator<EventResult> RESULT_COMPARATOR_BY_PLACE = (o1, o2) -> {
        if (o1.getPlace() == null) {
            return -1;
        }
        if (o2.getPlace() == null) {
            return -1;
        }
        if (o1.getPlace() < o2.getPlace()) {
            return 1;
        }
        if (o1.getPlace() > o2.getPlace()) {
            return -1;
        }
        return 0;
    };

    private Long eventId;

    private String eventName;

    private Integer place;

    private Integer attendeesInCategory;

    private Integer attendeesInCategoryWithDisq;

    private Integer time;

    private Integer loss;

    private String category;

    private LocalDate date;

    public EventResult(Long eventId, String eventName, Integer place, Integer attendeesInCategory, Integer attendeesInCategoryWithDisq, Integer time, Integer loss, String category, LocalDate date) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.place = place;
        this.attendeesInCategory = attendeesInCategory;
        this.attendeesInCategoryWithDisq = attendeesInCategoryWithDisq;
        this.time = time;
        this.loss = loss;
        this.category = category;
        this.date = date;
    }
}