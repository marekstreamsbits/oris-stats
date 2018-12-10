package oris.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import oris.extractor.EventDetailOrisExtractor;
import oris.extractor.EventListOrisExtractor;
import oris.extractor.EventResultsOrisExtractor;
import oris.extractor.response.DataExtractor;
import oris.extractor.response.ResultDTO;
import oris.model.db.*;

import java.time.LocalDate;
import java.util.Collection;

@Log4j2
@Service
public class OrisApiExtractionService { //Possibility to easily hide behind interface if different access possible.

    private final RestTemplate restTemplate;

    public OrisApiExtractionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * For OB only. All events.
     * Id & Version info only!
     *
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public Collection<EventLite> getEvents(LocalDate dateFrom, LocalDate dateTo) {
        final EventListOrisExtractor eventListOrisExtractor = new EventListOrisExtractor();
        eventListOrisExtractor.withFromDate(dateFrom);
        eventListOrisExtractor.withToDate(dateTo);

        log.debug("Requesting getEvents from ORIS for dates from {} to {}", dateFrom, dateTo);
        final DataExtractor<Collection<EventLite>> dataExtractor = restTemplate.getForObject(eventListOrisExtractor.url(), eventListOrisExtractor.dataExtractor());

        return dataExtractor.getData();
    }

    public Event getEventDetail(Long eventId) {
        EventDetailOrisExtractor detailOrisExtractor = new EventDetailOrisExtractor();
        detailOrisExtractor.withEventId(eventId);

        log.debug("Requesting getEventDetail from ORIS for event {}", eventId);
        DataExtractor<Event> eventDataExtractor = restTemplate.getForObject(detailOrisExtractor.url(), detailOrisExtractor.dataExtractor());

        return eventDataExtractor.getData();
    }

    public Collection<ResultDTO> getEventResults(Long eventId) {
        EventResultsOrisExtractor eventResultsExtractor = new EventResultsOrisExtractor();
        eventResultsExtractor.withEventId(eventId);

        log.debug("Requesting getEventResults from ORIS for event {}", eventId);
        DataExtractor<Collection<ResultDTO>> resultsExtractor = restTemplate.getForObject(eventResultsExtractor.url(), eventResultsExtractor.dataExtractor());

        return resultsExtractor.getData();
    }
}