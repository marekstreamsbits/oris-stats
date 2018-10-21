package oris.model.api;

import lombok.Data;

import java.util.Date;

@Data
public class EventResult {

    private Long eventId;

    private String eventName;

    private Integer place;

    private Integer attendeesInCategory;

    private Integer attendeesInCategoryWithDisq;

    private Integer time;

    private Integer loss;

    private String category;

    private Date date;

    public EventResult(Long eventId, String eventName, Integer place, Integer attendeesInCategory, Integer attendeesInCategoryWithDisq, Integer time, Integer loss, String category, Date date) {
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