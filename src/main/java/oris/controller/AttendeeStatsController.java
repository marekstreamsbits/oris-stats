package oris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import oris.model.api.AttendeeStatistics;
import oris.service.StatisticsService;

@RestController
public class AttendeeStatsController extends BaseOrisStatsController {

    private final StatisticsService statisticsService;

    private static final Logger LOG = LoggerFactory.getLogger(AttendeeStatsController.class);

    public AttendeeStatsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/stats/attendee/{registrationNumber}", method = RequestMethod.GET, produces = "application/json")
    public AttendeeStatistics attendeeStatistics(@PathVariable(value = "registrationNumber") String registrationNumber) {
        LOG.debug("GET for /stats/attendee/{registrationNumber} with param {}", registrationNumber);
        return statisticsService.getAttendeeStatistics(registrationNumber);
    }
}