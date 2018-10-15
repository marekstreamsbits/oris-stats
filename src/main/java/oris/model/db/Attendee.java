package oris.model.db;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
public class Attendee extends BaseEntity {

    @Column(unique = true)
    private String registrationNumber;
}