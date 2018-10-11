package oris.model.api;

import java.util.Date;

public class EventResult {

    private Long eventId;

    private String eventName;

    private Integer place;

    private Integer attendeesInCategory;

    private Integer getAttendeesInCategoryWithDisq;

    private Integer time;

    private Integer loss;

    private String category;

    private Date date;

    public EventResult(Long eventId, String eventName, Integer place, Integer attendeesInCategory, Integer getAttendeesInCategoryWithDisq, Integer time, Integer loss, String category, Date date) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.place = place;
        this.attendeesInCategory = attendeesInCategory;
        this.getAttendeesInCategoryWithDisq = getAttendeesInCategoryWithDisq;
        this.time = time;
        this.loss = loss;
        this.category = category;
        this.date = date;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getPlace() {
        return place;
    }

    public void setPlace(Integer place) {
        this.place = place;
    }

    public Integer getAttendeesInCategory() {
        return attendeesInCategory;
    }

    public void setAttendeesInCategory(Integer attendeesInCategory) {
        this.attendeesInCategory = attendeesInCategory;
    }

    public Integer getGetAttendeesInCategoryWithDisq() {
        return getAttendeesInCategoryWithDisq;
    }

    public void setGetAttendeesInCategoryWithDisq(Integer getAttendeesInCategoryWithDisq) {
        this.getAttendeesInCategoryWithDisq = getAttendeesInCategoryWithDisq;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getLoss() {
        return loss;
    }

    public void setLoss(Integer loss) {
        this.loss = loss;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}