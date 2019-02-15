package oris.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import oris.model.api.EventResult;
import oris.service.EventService;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
public class EventsStatsController extends BaseOrisStatsController {

    private final EventService eventService;

    public EventsStatsController(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping(value = "/stats/event/{eventId}/results", method = RequestMethod.GET, produces = "application/json")
    public Map<String, List<EventResult>> findResultsForEvent(@PathVariable(value = "eventId") final Long eventId) {
        log.debug("GET for /stats/event/{eventId}/results with param {}", eventId);
        final Map<String, List<EventResult>> eventResults = eventService.getResultsForEvent(eventId);
        return eventResults;
    }
}