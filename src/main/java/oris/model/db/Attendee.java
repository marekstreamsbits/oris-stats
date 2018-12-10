package oris.model.db;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Table(name = "attendees")
@Entity
public class Attendee extends BaseEntity {

    @Column(unique = true)
    private String registrationNumber;
}