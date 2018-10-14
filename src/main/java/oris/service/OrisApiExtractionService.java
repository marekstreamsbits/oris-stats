package oris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class OrisApiExtractionService { //Possibility to easily hide behind interface if different access possible.

    private final RestTemplate restTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(OrisApiExtractionService.class);

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
        EventListOrisExtractor eventListOrisExtractor = new EventListOrisExtractor();
        eventListOrisExtractor.withFromDate(dateFrom);
        eventListOrisExtractor.withToDate(dateTo);

        LOG.debug("Requesting getEvents from ORIS for dates from {} to {}", dateFrom, dateTo);
        DataExtractor<Collection<EventLite>> dataExtractor = restTemplate.getForObject(eventListOrisExtractor.url(), eventListOrisExtractor.dataExtractor());

        return dataExtractor.getData();
    }

    public Event getEventDetail(Long eventId) {
        EventDetailOrisExtractor detailOrisExtractor = new EventDetailOrisExtractor();
        detailOrisExtractor.withEventId(eventId);

        LOG.debug("Requesting getEventDetail from ORIS for event {}", eventId);
        DataExtractor<Event> eventDataExtractor = restTemplate.getForObject(detailOrisExtractor.url(), detailOrisExtractor.dataExtractor());

        return eventDataExtractor.getData();
    }

    public Collection<ResultDTO> getEventResults(Long eventId) {
        EventResultsOrisExtractor eventResultsExtractor = new EventResultsOrisExtractor();
        eventResultsExtractor.withEventId(eventId);

        LOG.debug("Requesting getEventResults from ORIS for event {}", eventId);
        DataExtractor<Collection<ResultDTO>> resultsExtractor = restTemplate.getForObject(eventResultsExtractor.url(), eventResultsExtractor.dataExtractor());

        return resultsExtractor.getData();
    }
}