package cwrm.controller;

import cwrm.api.NominatimAddressLookup;
import cwrm.api.NominatimAddressLookup.LookupResult;
import cwrm.model.Address;
import cwrm.model.CollectionLog;
import cwrm.model.Collector;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import cwrm.service.PickupService;
import cwrm.service.RewardRule;
import cwrm.service.RewardService;
import cwrm.service.StatisticsService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The controller in MVC: the only class the Swing views talk to.
 *
 * <p>It checks every raw value the user typed, calls the services, and returns an
 * {@link Outcome} with a friendly message. Errors thrown by the services
 * ({@link IllegalArgumentException} and {@link IllegalStateException}) become failed
 * outcomes, so no exception ever reaches a view.
 */
public class PickupController {
    private static final double MAX_WEIGHT_KG = 1000.0;
    private static final double UNKNOWN_COORDINATE = 0.0;

    private static final String CHOOSE_RESIDENT = "Please choose a resident.";
    private static final String ENTER_ADDRESS = "Please enter an address.";
    private static final String CHOOSE_MATERIAL = "Please choose a material.";
    private static final String ENTER_DATE = "Please enter the date as yyyy-MM-dd, for example 2026-12-01.";
    private static final String DATE_IN_PAST = "The pickup date cannot be in the past.";
    private static final String WEIGHT_NOT_NUMBER = "Weight must be a number of kilograms, for example 5 or 2.5.";
    private static final String WEIGHT_OUT_OF_RANGE = "Weight must be more than 0 kg and at most %.0f kg.";
    private static final String ENTER_WEIGHT = "Please enter the collected weight in kg.";
    private static final String SELECT_PICKUP = "Please select a pickup first.";
    private static final String CHOOSE_COLLECTOR = "Please choose a collector.";
    private static final String CHOOSE_RULE = "Please choose a reward rule.";
    private static final String BOOKED = "Pickup %s booked for %s. Status: %s.";
    private static final String COMPLETED = "Pickup %s completed. %d points credited to %s.";
    private static final String CANCELLED = "Pickup %s cancelled.";
    private static final String RULE_CHANGED = "Reward rule changed to: %s.";

    private final PickupService pickups;
    private final RewardService rewards;
    private final StatisticsService statistics;
    private final NominatimAddressLookup addressLookup;
    private final List<Resident> residents;
    private final List<Collector> collectors;
    private final List<RewardRule> ruleOptions;
    private volatile Address foundAddress;

    /**
     * Creates the controller.
     *
     * @param pickups       stores and updates pickup requests
     * @param rewards       holds the active reward rule
     * @param addressLookup finds addresses with the Nominatim API
     * @param residents     the residents who can book pickups
     * @param collectors    the collectors who can complete pickups
     * @param ruleOptions   the reward rules an administrator can choose from
     */
    public PickupController(PickupService pickups, RewardService rewards, NominatimAddressLookup addressLookup,
                            List<Resident> residents, List<Collector> collectors, List<RewardRule> ruleOptions) {
        this.pickups = pickups;
        this.rewards = rewards;
        this.statistics = new StatisticsService(pickups);
        this.addressLookup = addressLookup;
        this.residents = List.copyOf(residents);
        this.collectors = List.copyOf(collectors);
        this.ruleOptions = List.copyOf(ruleOptions);
    }

    /**
     * Looks up an address with the Nominatim API. This sends a network request, so the
     * views call it from a background thread ({@code SwingWorker}), never the Swing event thread.
     *
     * @param addressText the address as typed
     * @return the address found, if any, and a friendly message
     */
    public LookupResult findAddress(String addressText) {
        if (isBlank(addressText)) {
            return new LookupResult(null, ENTER_ADDRESS);
        }
        LookupResult result = addressLookup.lookup(addressText.trim());
        if (result.hasAddress()) {
            foundAddress = result.address();
        }
        return result;
    }

    /**
     * Checks the booking form and books the pickup.
     *
     * @param form the values from the booking screen
     * @return success with the new request ID, or failure with what to fix
     */
    public Outcome bookPickup(BookingForm form) {
        try {
            PickupRequest request = toRequest(form);
            pickups.book(request);
            return Outcome.ok(String.format(BOOKED, request.getId(), request.getResident().getName(),
                    request.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException invalid) {
            return Outcome.fail(invalid.getMessage());
        }
    }

    private PickupRequest toRequest(BookingForm form) {
        require(form.resident() != null, CHOOSE_RESIDENT);
        require(!isBlank(form.addressText()), ENTER_ADDRESS);
        require(form.material() != null, CHOOSE_MATERIAL);
        PickupRequest.Builder builder = new PickupRequest.Builder()
                .resident(form.resident())
                .address(addressFor(form.addressText().trim()))
                .material(form.material())
                .date(parseDate(form.dateText()))
                .notes(form.notes())
                .timeSlot(form.timeSlot());
        if (!isBlank(form.weightText())) {
            builder.estimatedWeightKg(parseWeight(form.weightText()));
        }
        return builder.id(pickups.nextRequestId()).build();
    }

    /** Uses the coordinates from "Find address" if the text still matches the address found. */
    private Address addressFor(String addressText) {
        Address found = foundAddress;
        if (found != null && found.displayName().equals(addressText)) {
            return found;
        }
        return new Address(addressText, UNKNOWN_COORDINATE, UNKNOWN_COORDINATE);
    }

    /**
     * Records that a collector has collected a pending pickup, and credits the resident's points.
     *
     * @param pickupId   the selected pickup's ID, or {@code null} if none is selected
     * @param collector  the chosen collector, or {@code null} if none is chosen
     * @param weightText the weight actually collected, in kg, as typed
     * @return success with the points credited, or failure with what to fix
     */
    public Outcome completePickup(String pickupId, Collector collector, String weightText) {
        try {
            requireSelection(pickupId, collector);
            require(!isBlank(weightText), ENTER_WEIGHT);
            CollectionLog log = pickups.complete(pickupId, collector, parseWeight(weightText));
            String residentName = pickups.find(pickupId).getResident().getName();
            return Outcome.ok(String.format(COMPLETED, pickupId, log.pointsAwarded(), residentName));
        } catch (IllegalArgumentException | IllegalStateException invalid) {
            return Outcome.fail(invalid.getMessage());
        }
    }

    /**
     * Cancels a pending pickup.
     *
     * @param pickupId  the selected pickup's ID, or {@code null} if none is selected
     * @param collector the chosen collector, or {@code null} if none is chosen
     * @return success, or failure with what to fix
     */
    public Outcome cancelPickup(String pickupId, Collector collector) {
        try {
            requireSelection(pickupId, collector);
            pickups.cancel(pickupId);
            return Outcome.ok(String.format(CANCELLED, pickupId));
        } catch (IllegalArgumentException | IllegalStateException invalid) {
            return Outcome.fail(invalid.getMessage());
        }
    }

    /**
     * Switches the reward rule used for future completions.
     *
     * @param rule one of {@link #ruleOptions()}, or {@code null} if none is chosen
     * @return success with the new rule's name, or failure if no rule was chosen
     */
    public Outcome changeRule(RewardRule rule) {
        if (rule == null) {
            return Outcome.fail(CHOOSE_RULE);
        }
        rewards.setRule(rule);
        return Outcome.ok(String.format(RULE_CHANGED, rule.getName()));
    }

    /** @return the reward rule currently in use */
    public RewardRule activeRule() {
        return rewards.getRule();
    }

    /** @return the reward rules an administrator can choose from */
    public List<RewardRule> ruleOptions() {
        return ruleOptions;
    }

    /** @return the residents who can book pickups */
    public List<Resident> residents() {
        return residents;
    }

    /** @return the collectors who can complete pickups */
    public List<Collector> collectors() {
        return collectors;
    }

    /** @return every pickup request, oldest first */
    public List<PickupRequest> allPickups() {
        return pickups.listAll();
    }

    /** @return the pickups still waiting to be collected, oldest first */
    public List<PickupRequest> pendingPickups() {
        return pickups.listAll().stream()
                .filter(request -> request.getStatus() == PickupStatus.PENDING)
                .toList();
    }

    /**
     * Lists one resident's pickups.
     *
     * @param resident the resident
     * @return that resident's pickups, oldest first
     */
    public List<PickupRequest> pickupsFor(Resident resident) {
        return pickups.listFor(resident);
    }

    /** @return every collection log entry, oldest first */
    public List<CollectionLog> logs() {
        return pickups.getLogs();
    }

    /** @return completion rate, average turnaround and kilograms per material */
    public StatisticsSummary statistics() {
        return new StatisticsSummary(statistics.completionRate(), statistics.averageTurnaround(),
                statistics.totalKgByMaterial());
    }

    private static LocalDate parseDate(String dateText) {
        require(!isBlank(dateText), ENTER_DATE);
        LocalDate date;
        try {
            date = LocalDate.parse(dateText.trim());
        } catch (DateTimeParseException badFormat) {
            throw new IllegalArgumentException(ENTER_DATE);
        }
        require(!date.isBefore(LocalDate.now()), DATE_IN_PAST);
        return date;
    }

    private static double parseWeight(String weightText) {
        double kg;
        try {
            kg = Double.parseDouble(weightText.trim());
        } catch (NumberFormatException notANumber) {
            throw new IllegalArgumentException(WEIGHT_NOT_NUMBER);
        }
        boolean inRange = Double.isFinite(kg) && kg > 0 && kg <= MAX_WEIGHT_KG;
        require(inRange, String.format(WEIGHT_OUT_OF_RANGE, MAX_WEIGHT_KG));
        return kg;
    }

    private static void requireSelection(String pickupId, Collector collector) {
        require(!isBlank(pickupId), SELECT_PICKUP);
        require(collector != null, CHOOSE_COLLECTOR);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
