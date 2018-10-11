package oris.model.db;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Table(indexes = {
        @Index(name = "event_category_index", columnList = "event_id, category")
})
@Entity
public class EventStatistics extends BaseEntity {

    private String category;

    @ManyToOne
    private Event event;

    private int attendees;

    //All attendees
    private int attendeesWithDisqualified;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getAttendees() {
        return attendees;
    }

    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }

    public int getAttendeesWithDisqualified() {
        return attendeesWithDisqualified;
    }

    public void setAttendeesWithDisqualified(int attendeesWithDisqualified) {
        this.attendeesWithDisqualified = attendeesWithDisqualified;
    }
}