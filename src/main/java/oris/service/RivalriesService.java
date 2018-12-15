package oris.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import oris.model.api.GlobalRivalriesRequestFilter;
import oris.model.api.GlobalRivalryDto;
import oris.model.api.PagedResult;
import oris.repository.EventRivalryRepository;
import oris.repository.GlobalRivalryRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class RivalriesService {

    private final EventRivalryRepository eventRivalryRepository;

    private final GlobalRivalryRepository globalRivalryRepository;

    public PagedResult<GlobalRivalryDto> getGlobalRivalries(final String registrationNumber, final GlobalRivalriesRequestFilter filter) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<GlobalRivalryDto> getGlobalRivalriesForSpecificRival(final String registrationNumber, final String rivalRegistrationNumber) {
        return globalRivalryRepository.findAllByAttendeeRegistrationNumberAndRivalRegistrationNumber(registrationNumber, rivalRegistrationNumber);
    }

    public List<GlobalRivalryDto> getGlobalRivalries(String registrationNumber) {
        return globalRivalryRepository.findAllByAttendeeRegistrationNumber(registrationNumber);
    }
}