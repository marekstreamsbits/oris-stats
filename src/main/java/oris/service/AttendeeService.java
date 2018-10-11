package oris.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import oris.model.db.Attendee;
import oris.repository.AttendeeRepository;

@Service
public class AttendeeService {

    private AttendeeRepository attendeeRepository;

    @Autowired
    public AttendeeService(AttendeeRepository attendeeRepository) {
        this.attendeeRepository = attendeeRepository;
    }

    @Cacheable(value = "attendees", key = "#registrationNumber")
    public Attendee findOrCreate(String registrationNumber) {

        synchronized (registrationNumber.intern()) {
            Attendee attendee = attendeeRepository.findByRegistrationNumber(registrationNumber);
            if (attendee == null) {
                attendee = new Attendee();
            } else {
                return attendee;
            }
            attendee.setRegistrationNumber(registrationNumber);
            attendee.setRegistrationNumber(registrationNumber);
            return attendeeRepository.save(attendee);
        }
    }
}