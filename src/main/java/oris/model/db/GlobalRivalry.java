package oris.model.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "global_rivalries", indexes = {@Index(name = "IDX_GLOBAL_RIVALRIES_CATEGORY", columnList = "category"),
        @Index(name = "IDX_GLOBAL_RIVALRIES_WIN_DIFFERENCE", columnList = "winDifference"),
        @Index(name = "IDX_GLOBAL_RIVALRIES_WIN_DIFFERENCE_ABS", columnList = "winDifferenceAbs"),
        @Index(name = "IDX_GLOBAL_RIVALRIES_EVENTS_COUNT", columnList = "eventsCount")})
public class GlobalRivalry extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private Attendee attendee;

    @OneToOne(fetch = FetchType.LAZY)
    private Attendee rival;

    private String category;

    /** Times attendee was better than their rival */
    private int winDifference;

    /** Times attendee was better than their rival - absolute value for search purposes. */
    private int winDifferenceAbs;

    /** How many events in the given category have attendee and their rival participated in together. */
    private int eventsCount;
}