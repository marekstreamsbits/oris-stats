package oris.model.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "event_rivalries", indexes = {
        @Index(name = "IDX_EVENT_RIVALRY_CATEGORY", columnList = "category"),
        @Index(name = "IDX_EVENT_RIVALRY_ATTENDEE", columnList = "attendee_id"),
        @Index(name = "IDX_EVENT_RIVALRY_ATTENDEE_RIVAL", columnList = "attendee_id, rival_id"),
        @Index(name = "IDX_EVENT_RIVALRY_EVENT", columnList = "event_id")
})
@Entity
public class EventRivalry extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private Attendee attendee;

    @OneToOne(fetch = FetchType.LAZY)
    private Attendee rival;

    @OneToOne(fetch = FetchType.LAZY)
    private Event event;

    private String category;

    private boolean attendeeDisqualified;

    private boolean rivalDisqualified;

    // Null in case both were disqualified.
    private Boolean rivalWon;

    private Integer attendeePlace;

    private Integer rivalPlace;

    private Integer attendeeTime;

    private Integer rivalTime;
}