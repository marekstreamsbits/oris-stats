package oris.controller;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import oris.model.api.GlobalRivalriesRequestFilter;
import oris.model.api.GlobalRivalryDto;
import oris.model.api.PagedResult;
import oris.service.RivalriesService;

import java.util.List;

@AllArgsConstructor
@Log4j2
@RestController
public class RivalriesStatsController extends BaseOrisStatsController {

    private final RivalriesService rivalriesService;

    @RequestMapping(value = "/stats/rivalries/{registrationNumber}", method = RequestMethod.GET, produces = "application/json")
    public PagedResult<GlobalRivalryDto> globalRivalriesFiltered(@PathVariable(value = "registrationNumber") String registrationNumber, GlobalRivalriesRequestFilter filter) {
        log.debug("GET for /stats/rivalries/{registrationNumber} for {} with params {}", registrationNumber, filter);
        final PagedResult<GlobalRivalryDto> globalRivalries = rivalriesService.getGlobalRivalries(registrationNumber, filter);
        return globalRivalries;
    }

    @RequestMapping(value = "/stats/rivalries/{registrationNumber}/{rivalRegistrationNumber}", method = RequestMethod.GET, produces = "application/json")
    public List<GlobalRivalryDto> attendeeStatistics(@PathVariable(value = "registrationNumber") String registrationNumber,
                                                     @PathVariable(value = "rivalRegistrationNumber") String rivalRegistrationNumber) {
        log.debug("GET for /stats/rivalries/{registrationNumber}/{rivalRegistrationNumber} for {} and {}", registrationNumber, rivalRegistrationNumber);
        final List<GlobalRivalryDto> globalRivalries = rivalriesService.getGlobalRivalriesForSpecificRival(registrationNumber, rivalRegistrationNumber);
        return globalRivalries;
    }

    @RequestMapping(value = "/stats/rivalries/{registrationNumber}/all", method = RequestMethod.GET, produces = "application/json")
    public List<GlobalRivalryDto> globalRivalriesFiltered(@PathVariable(value = "registrationNumber") String registrationNumber) {
        log.debug("GET for /stats/rivalries/{registrationNumber}/all for {} with params {}", registrationNumber);
        final List<GlobalRivalryDto> globalRivalries = rivalriesService.getGlobalRivalries(registrationNumber);
        return globalRivalries;
    }
}