package oris.model.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "event_rivalries", indexes = {@Index(name = "IDX_EVENT_RIVALRY_CATEGORY", columnList = "category")})
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