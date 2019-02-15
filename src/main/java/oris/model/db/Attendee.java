package oris.model.db;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Table(name = "attendees",
        indexes = @Index(name = "IDX_ATTENDEE_REG_NO", columnList = "registrationNumber"))
@Entity
public class Attendee extends BaseEntity {

    @Column(unique = true)
    private String registrationNumber;
}