package oris.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oris.model.api.AttendeeStatistics;
import oris.repository.ResultRepository;

@Service
public class StatisticsService {

    private ResultRepository resultRepository;

    @Autowired
    public StatisticsService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public AttendeeStatistics getAttendeeStatistics(String registrationNumber) {
        final AttendeeStatistics attendeeStatistics = new AttendeeStatistics();
        attendeeStatistics.setRegNo(registrationNumber);
        attendeeStatistics.setEventResults(resultRepository.getStatisticsForAttendee(registrationNumber));

        return attendeeStatistics;
    }
}