package cwrm.service;

import cwrm.model.CollectionLog;
import cwrm.model.Collector;
import cwrm.model.PickupRequest;
import cwrm.model.Resident;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all pickup requests and handles their life cycle:
 * booking, cancelling and completing.
 *
 * <p>Completing a pickup calculates the reward points with {@link RewardService},
 * credits them to the resident and writes a {@link CollectionLog}.
 * Data is held in memory; this class has no GUI code, so the maps could later be
 * replaced by a database repository without changing the controller or views.
 */
public class PickupService {
    private static final String ID_FORMAT = "PU-%03d";
    private static final String MISSING_REQUEST = "A pickup request is required.";
    private static final String DUPLICATE_ID = "A pickup with ID %s already exists.";
    private static final String UNKNOWN_ID = "No pickup found with ID %s.";
    private static final String MISSING_COLLECTOR = "Please choose a collector.";
    private static final String INVALID_WEIGHT = "The collected weight must be greater than 0 kg.";

    private final RewardService rewards;
    private final Map<String, PickupRequest> requests = new LinkedHashMap<>();
    private final List<CollectionLog> logs = new ArrayList<>();
    private int nextId = 1;

    /**
     * Creates the service.
     *
     * @param rewards calculates the points awarded when a pickup is completed
     */
    public PickupService(RewardService rewards) {
        this.rewards = rewards;
    }

    /**
     * Hands out the next free request ID. Each call returns a new ID.
     *
     * @return the next ID, for example {@code PU-001}, then {@code PU-002}
     */
    public String nextRequestId() {
        String id = String.format(ID_FORMAT, nextId);
        nextId++;
        return id;
    }

    /**
     * Stores a new pickup request.
     *
     * @param request the request to store, built with {@link PickupRequest.Builder}
     * @throws IllegalArgumentException if the request is missing or its ID is already used
     */
    public void book(PickupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(MISSING_REQUEST);
        }
        if (requests.containsKey(request.getId())) {
            throw new IllegalArgumentException(String.format(DUPLICATE_ID, request.getId()));
        }
        requests.put(request.getId(), request);
    }

    /**
     * Looks up a request by its ID.
     *
     * @param id the request ID
     * @return the request
     * @throws IllegalArgumentException if no request has this ID
     */
    public PickupRequest find(String id) {
        PickupRequest request = requests.get(id);
        if (request == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_ID, id));
        }
        return request;
    }

    /**
     * Cancels a pending pickup.
     *
     * @param id the request ID
     * @throws IllegalArgumentException if no request has this ID
     * @throws IllegalStateException    if the pickup is not pending
     */
    public void cancel(String id) {
        find(id).cancel();
    }

    /**
     * Completes a pending pickup: marks it completed, credits the resident's points
     * and writes a collection log. Nothing changes if any check fails.
     *
     * @param id        the request ID
     * @param collector the collector who collected the waste
     * @param kg        the weight actually collected, in kilograms; greater than 0
     * @return the new collection log entry
     * @throws IllegalArgumentException if the ID is unknown, the collector is missing or the weight is not positive
     * @throws IllegalStateException    if the pickup is not pending
     */
    public CollectionLog complete(String id, Collector collector, double kg) {
        PickupRequest request = find(id);
        requireCompletionDetails(collector, kg);
        int points = rewards.pointsFor(request.getMaterial(), kg);
        request.complete();
        request.getResident().getAccount().add(points);
        CollectionLog log = new CollectionLog(id, collector, kg, points, LocalDateTime.now());
        logs.add(log);
        return log;
    }

    private static void requireCompletionDetails(Collector collector, double kg) {
        if (collector == null) {
            throw new IllegalArgumentException(MISSING_COLLECTOR);
        }
        if (!Double.isFinite(kg) || kg <= 0) {
            throw new IllegalArgumentException(INVALID_WEIGHT);
        }
    }

    /**
     * Lists the requests booked by one resident, oldest first.
     *
     * @param resident the resident
     * @return that resident's requests; the list cannot be changed
     */
    public List<PickupRequest> listFor(Resident resident) {
        return requests.values().stream()
                .filter(request -> request.getResident().getId().equals(resident.getId()))
                .toList();
    }

    /** @return every request, oldest first; the list cannot be changed */
    public List<PickupRequest> listAll() {
        return List.copyOf(requests.values());
    }

    /** @return every collection log entry, oldest first; the list cannot be changed */
    public List<CollectionLog> getLogs() {
        return List.copyOf(logs);
    }
}
