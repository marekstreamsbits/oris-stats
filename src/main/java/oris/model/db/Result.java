package oris.model.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "results", indexes = {@Index(name = "IDX_RESULT_CATEGORY", columnList = "category")})
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