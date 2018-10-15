package oris.model.db;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Data
@Entity
public class Result extends BaseEntity {

    private Integer place;

    private Integer time;

    private Integer loss;

    private String category;

    @OneToOne(fetch = FetchType.LAZY)
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;
}