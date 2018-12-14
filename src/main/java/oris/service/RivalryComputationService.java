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
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RivalryComputationService {

    private final EventRivalryRepository eventRivalryRepository;
    private final GlobalRivalryRepository globalRivalryRepository;

    private static final int GLOBAL_RIVALRY_COMPUTATION_THREADS = Runtime.getRuntime().availableProcessors();

    // DB is a bottle neck here - we do not want to call persist() on little amounts of data, make it hundreds, thousands of records per call.
    private static final int MAX_ATTENDEES_DATA_PER_TRANSACTION = 20;

    public RivalryComputationService(EventRivalryRepository eventRivalryRepository, GlobalRivalryRepository globalRivalryRepository) {
        this.eventRivalryRepository = eventRivalryRepository;
        this.globalRivalryRepository = globalRivalryRepository;
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

    public void computeGlobalRivalriesInitial(final Collection<Long> attendeeIds) {
        try {
            dropIndexes();
            log.info("Creating completion service to compute global rivalries. Number of executor threads: {}", GLOBAL_RIVALRY_COMPUTATION_THREADS);
            final ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(GLOBAL_RIVALRY_COMPUTATION_THREADS));
            final Iterator<Long> attendeeIterator = attendeeIds.iterator();
            log.info("Computing global rivalries for {} attendees.", attendeeIds.size());
            int runningThreads = 0;
            while (attendeeIterator.hasNext()) {
                while (attendeeIterator.hasNext() && runningThreads < GLOBAL_RIVALRY_COMPUTATION_THREADS) {
                    final List<Long> attendeeIdsToProcess = takeIds(attendeeIterator, MAX_ATTENDEES_DATA_PER_TRANSACTION);
                    completionService.submit(() -> {
                        computeGlobalRivalriesForAttendee(attendeeIdsToProcess);
                        return true;
                    });
                    runningThreads++;
                }
                completionService.take().get();
                runningThreads--;
            }
            log.info("Finished computing global rivalries.");
            // If one thread fails, the whole thing goes up in flames.. we have no problem with that. We desire all data.
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while computing global rivalries.", e);
        } finally {
            createIndexes();
        }
    }

    public void computeGlobalRivalriesDaily(final Collection<Long> attendeeIds, final Collection<Long> eventIds) {
        try {
            log.info("Creating completion service to perform daily computation of global rivalries. Number of executor threads: {}", GLOBAL_RIVALRY_COMPUTATION_THREADS);
            final ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(GLOBAL_RIVALRY_COMPUTATION_THREADS));
            final Iterator<Long> attendeeIterator = attendeeIds.iterator();
            log.info("Computing daily global rivalries for {} attendees.", attendeeIds.size());
            int runningThreads = 0;
            while (attendeeIterator.hasNext()) {
                while (attendeeIterator.hasNext() && runningThreads < GLOBAL_RIVALRY_COMPUTATION_THREADS) {
                    completionService.submit(() -> {
                        computeGlobalRivalriesForAttendee(attendeeIterator.next(), eventIds);
                        return true;
                    });
                    runningThreads++;
                }
                completionService.take().get();
                runningThreads--;
            }
            log.info("Finished computing global rivalries.");
            // If one thread fails, the whole thing goes up in flames.. we have no problem with that. We desire all data.
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while computing global rivalries.", e);
        }
    }

    private void computeGlobalRivalriesForAttendee(Long attendeeId, Collection<Long> eventIds) {
        final Map<String, List<EventRivalry>> categoryToEventRivalries = eventRivalryRepository.findAllByAttendeeIdAndEventId(attendeeId, eventIds)
                .stream() // Doing this in rare case somebody runs multiple races in some day, even multiple in the same category.
                .collect(Collectors.groupingBy(EventRivalry::getCategory));
        categoryToEventRivalries.forEach((category, rivalries) -> {
            final Map<Long, GlobalRivalry> rivalIdToGlobalRivalry = globalRivalryRepository.findAllByAttendeeIdAndRivalIdAndCategory(attendeeId,
                    rivalries
                            .stream()
                            .map(eventRivalry -> eventRivalry.getRival().getId())
                            .collect(Collectors.toList()), category)
                    .stream()
                    .collect(Collectors.toMap(globalRivalry -> globalRivalry.getRival().getId(), globalRivalry -> globalRivalry));
            rivalries.forEach(eventRivalry -> {
                final Long rivalId = eventRivalry.getRival().getId();
                if (!rivalIdToGlobalRivalry.containsKey(rivalId)) {
                    rivalIdToGlobalRivalry.put(rivalId, createGlobalRivalry(List.of(eventRivalry)));
                }
                updateGlobalRivalry(eventRivalry, rivalIdToGlobalRivalry.get(rivalId));
            });
            globalRivalryRepository.saveAll(rivalIdToGlobalRivalry.values());
        });
    }

    private void updateGlobalRivalry(final EventRivalry eventRivalry, final GlobalRivalry globalRivalry) {


    }

    private void createIndexes() {
        log.info("Creating indexes on global_rivalries.");
        globalRivalryRepository.createIndexes();
        log.info("Successfully created indexes on global_rivalries.");
    }

    private void dropIndexes() {
        log.info("Dropping indexes on global_rivalries.");
        globalRivalryRepository.dropIndexes();
        log.info("Dropped indexes on global_rivalries.");
    }

    private void computeGlobalRivalriesForAttendee(final List<Long> attendeeIds) {
        final List<GlobalRivalry> globalRivalries = new ArrayList<>(attendeeIds.size() * 100);
        attendeeIds.forEach(attendeeId -> globalRivalries.addAll(computeGlobalRivalriesForAttendeeId(attendeeId)));
        globalRivalryRepository.saveAll(globalRivalries);
    }

    private List<GlobalRivalry> computeGlobalRivalriesForAttendeeId(final Long attendeeId) {
        final Collection<EventRivalry> eventRivalries = eventRivalryRepository.findAllByAttendeeId(attendeeId);
        final Map<Long, List<EventRivalry>> rivalToEventRivalriesMap = new HashMap<>();
        eventRivalries.forEach(eventRivalry -> {
            final Long rivalId = eventRivalry.getRival().getId();
            final List<EventRivalry> rivalries = rivalToEventRivalriesMap.getOrDefault(rivalId, new ArrayList<>());
            rivalries.add(eventRivalry);
            rivalToEventRivalriesMap.put(rivalId, rivalries);
        });
        final List<GlobalRivalry> globalRivalries = new ArrayList<>(eventRivalries.size());  // This is the maximum there can be global rivalries, even though unlikely.
        rivalToEventRivalriesMap.values().forEach(rivalries -> globalRivalries.addAll(computeAndPersistGlobalRivalries(rivalries, attendeeId)));
        return globalRivalries;
    }

    private List<GlobalRivalry> computeAndPersistGlobalRivalries(final List<EventRivalry> eventRivalries, final Long attendeeId) {
        if (eventRivalries.isEmpty()) {
            log.warn("Event Rivalries for attendee are unlikely to be empty. Please check for attendee {}.", attendeeId);
            return Collections.EMPTY_LIST;
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
                    final GlobalRivalry globalRivalry = createGlobalRivalry(rivalries);
                    globalRivalries.add(globalRivalry);
                }
        );
        return globalRivalries;
    }

    private GlobalRivalry createGlobalRivalry(List<EventRivalry> rivalries) {
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
        globalRivalry.setWinDifferenceAbs(Math.abs(globalRivalry.getWinDifference()));
        return globalRivalry;
    }

    private void createAndSaveRivalries(final List<Result> results) {
        final List<EventRivalry> rivalries = new ArrayList<>(Long.valueOf(Math.round(Math.pow(results.size(), 2))).intValue()); // approximation.. little higher than the real value
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

    private List<Long> takeIds(final Iterator<Long> attendeeIterator, final int max) {
        final List<Long> attendeeIds = new ArrayList<>(max);
        int counter = 0;
        while (attendeeIterator.hasNext() && counter < max) {
            attendeeIds.add(attendeeIterator.next());
            counter++;
        }
        return attendeeIds;
    }
}