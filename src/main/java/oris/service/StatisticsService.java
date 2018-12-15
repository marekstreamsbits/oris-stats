package oris.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import oris.model.api.AttendeeStatistics;
import oris.repository.ResultRepository;

@AllArgsConstructor
@Service
public class StatisticsService {

    private final ResultRepository resultRepository;

    public AttendeeStatistics getAttendeeStatistics(String registrationNumber) {
        final AttendeeStatistics attendeeStatistics = new AttendeeStatistics();
        attendeeStatistics.setRegNo(registrationNumber);
        attendeeStatistics.setEventResults(resultRepository.getStatisticsForAttendee(registrationNumber));

        return attendeeStatistics;
    }
}