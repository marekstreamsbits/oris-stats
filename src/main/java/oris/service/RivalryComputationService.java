package oris.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import oris.model.db.Event;
import oris.model.db.EventRivalry;
import oris.model.db.GlobalRivalry;
import oris.model.db.Result;
import oris.repository.EventRivalryRepository;
import oris.repository.GlobalRivalryRepository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Log4j2
@Service
public class RivalryComputationService {

    private final EventRivalryRepository eventRivalryRepository;
    private final GlobalRivalryRepository globalRivalryRepository;

    private static final int GLOBAL_RIVALRY_COMPUTATION_THREADS = 4;

    public RivalryComputationService(EventRivalryRepository eventRivalryRepository, GlobalRivalryRepository globalRivalryRepository) {
        this.eventRivalryRepository = eventRivalryRepository;
        this.globalRivalryRepository = globalRivalryRepository;
    }

    public void computeGlobalRivalries(final Collection<Long> attendeeIds) {
        try {
            log.info("Computing global rivalries for {} attendees.", attendeeIds.size());
            final ForkJoinPool pool = new ForkJoinPool(GLOBAL_RIVALRY_COMPUTATION_THREADS);
            pool.submit(() -> attendeeIds.parallelStream().forEach(this::computeGlobalRivalries)).get();
            log.info("Finished computing global rivalries.");
        } catch (InterruptedException e) {
            log.error("Error while computing global rivalries.", e);
        } catch (ExecutionException e) {
            log.error("Error while computing global rivalries.", e);
        }
    }

    private void computeGlobalRivalries(final Long attendeeId) {
        final Collection<EventRivalry> eventRivalries = eventRivalryRepository.findAllByAttendeeId(attendeeId);
        final Map<Long, List<EventRivalry>> rivalToEventRivalriesMap = new HashMap<>();
        eventRivalries.forEach(eventRivalry -> {
            final Long rivalId = eventRivalry.getRival().getId();
            final List<EventRivalry> rivalries = rivalToEventRivalriesMap.getOrDefault(rivalId, new ArrayList<>());
            rivalries.add(eventRivalry);
            rivalToEventRivalriesMap.put(rivalId, rivalries);
        });
        rivalToEventRivalriesMap.values().forEach(rivalries -> computeAndPersistGlobalRivalries(rivalries, attendeeId));
    }

    private void computeAndPersistGlobalRivalries(final List<EventRivalry> eventRivalries, final Long attendeeId) {
        if (eventRivalries.isEmpty()) {
            log.warn("Event Rivalries for attendee are unlikely to be empty. Please check for attendee {}.", attendeeId);
            return;
        }
        final Map<String, List<EventRivalry>> categoryToEventRivalries = new HashMap<>();
        eventRivalries.forEach(eventRivalry -> {
            final String category = eventRivalry.getCategory();
            final List<EventRivalry> rivalries = categoryToEventRivalries.getOrDefault(category, new ArrayList<>());
            rivalries.add(eventRivalry);
            categoryToEventRivalries.put(category,
                    categoryToEventRivalries.getOrDefault(category, rivalries));
        });
        final List<GlobalRivalry> globalRivalries = new ArrayList<>();
        categoryToEventRivalries.values().forEach(rivalries -> {
                    final GlobalRivalry globalRivalry = new GlobalRivalry();
                    globalRivalry.setAttendee(rivalries.get(0).getAttendee());
                    globalRivalry.setRival(rivalries.get(0).getRival());
                    globalRivalry.setCategory(rivalries.get(0).getCategory());
                    globalRivalry.setEventsCount(rivalries.size());
                    rivalries.forEach(rivalry -> {
                        if (rivalry.getRivalWon() != null) {
                            if (rivalry.getRivalWon()) {
                                globalRivalry.setWinDifference(globalRivalry.getWinDifference() - 1);
                            } else {
                                globalRivalry.setWinDifference(globalRivalry.getWinDifference() + 1);
                            }
                        }
                    });
                    globalRivalries.add(globalRivalry);
                }
        );
        globalRivalryRepository.saveAll(globalRivalries);
    }

    public void computeAndPersistRivalries(final Collection<Event> events) {
        events.forEach(
                event -> {
                    final Map<String, List<Result>> categoryResults = new HashMap<>();
                    event.getResults().forEach(result -> {
                        if (categoryResults.containsKey(result.getCategory())) {
                            categoryResults.get(result.getCategory()).add(result);
                        } else {
                            categoryResults.put(result.getCategory(), new ArrayList<>(List.of(result)));
                        }
                    });
                    categoryResults.values().forEach(this::createAndSaveRivalries);
                }
        );
    }

    private void createAndSaveRivalries(final List<Result> results) {
        final List<EventRivalry> rivalries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            for (int j = i + 1; j < results.size(); j++) {
                addTwoWayRivalry(rivalries, results.get(i), results.get(j));
            }
        }
        eventRivalryRepository.saveAll(rivalries);
    }

    private void addTwoWayRivalry(List<EventRivalry> rivalries, Result resultOne, Result resultTwo) {
        final EventRivalry eventRivalryOne = createBaseRivalry(resultOne);
        fillInRivalInfo(eventRivalryOne, resultTwo);

        final EventRivalry eventRivalryTwo = createBaseRivalry(resultTwo);
        fillInRivalInfo(eventRivalryTwo, resultOne);

        rivalries.add(eventRivalryOne);
        rivalries.add(eventRivalryTwo);
    }

    private void fillInRivalInfo(EventRivalry eventRivalry, Result result) {
        eventRivalry.setRival(result.getAttendee());
        eventRivalry.setRivalDisqualified(result.getPlace() == null);
        eventRivalry.setRivalPlace(result.getPlace());
        eventRivalry.setRivalTime(result.getTime());
        eventRivalry.setRivalWon(isRivalWinning(eventRivalry.getAttendeePlace(), result.getPlace()));
    }

    private EventRivalry createBaseRivalry(Result result) {
        final EventRivalry eventRivalry = new EventRivalry();
        eventRivalry.setAttendee(result.getAttendee());
        eventRivalry.setAttendeeDisqualified(result.getPlace() == null);
        eventRivalry.setCategory(result.getCategory());
        eventRivalry.setEvent(result.getEvent());
        eventRivalry.setAttendeePlace(result.getPlace());
        eventRivalry.setAttendeeTime(result.getTime());
        return eventRivalry;
    }

    private Boolean isRivalWinning(Integer attendeePlace, Integer rivalPlace) {
        if (attendeePlace != null && rivalPlace == null) {
            return false;
        }
        if (attendeePlace == null && rivalPlace != null) {
            return true;
        }
        if (attendeePlace == null && rivalPlace == null) {
            return null;
        }
        return rivalPlace < attendeePlace;
    }
}