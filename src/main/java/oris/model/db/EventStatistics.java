package oris.model.db;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Table(name = "event_statistics",
        indexes = {
                @Index(name = "event_category_index", columnList = "event_id, category")
        })
@Entity()
public class EventStatistics extends BaseEntity {

    private String category;

    @ManyToOne
    private Event event;

    private int attendees;

    // All attendees
    private int attendeesWithDisqualified;
}