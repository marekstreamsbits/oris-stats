package oris.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Attendee extends BaseEntity {

    @Column(unique = true)
    private String registrationNumber;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
}