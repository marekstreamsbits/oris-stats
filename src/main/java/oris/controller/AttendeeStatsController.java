package oris.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import oris.model.api.AttendeeStatistics;
import oris.service.StatisticsService;

@Log4j2
@RestController
public class AttendeeStatsController extends BaseOrisStatsController {

    private final StatisticsService statisticsService;

    public AttendeeStatsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/stats/attendee/{registrationNumber}", method = RequestMethod.GET, produces = "application/json")
    public AttendeeStatistics attendeeStatistics(@PathVariable(value = "registrationNumber") String registrationNumber) {
        log.debug("GET for /stats/attendee/{registrationNumber} with param {}", registrationNumber);
        AttendeeStatistics attendeeStatistics = statisticsService.getAttendeeStatistics(registrationNumber);
        return attendeeStatistics;
    }
}