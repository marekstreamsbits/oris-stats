package oris.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oris.model.db.Attendee;

@Repository
public interface AttendeeRepository extends CrudRepository<Attendee, Long> {

    Attendee findByRegistrationNumber(String registrationNumber);
}