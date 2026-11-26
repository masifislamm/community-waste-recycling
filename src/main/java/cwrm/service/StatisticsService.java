package cwrm.service;

import cwrm.model.CollectionLog;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Works out the figures the municipal administrator uses to judge how well the
 * pilot is going: completion rate, average turnaround, kilograms collected per material,
 * pickups per status and reward points given out.
 *
 * <p>It only reads data from {@link PickupService}; it never changes it.
 */
public class StatisticsService {
    private static final double PERCENT = 100.0;

    private final PickupService pickups;

    /**
     * Creates the service.
     *
     * @param pickups where the requests and collection logs are read from
     */
    public StatisticsService(PickupService pickups) {
        this.pickups = pickups;
    }

    /**
     * Share of all booked pickups that have been completed. Cancelled pickups count as not completed.
     *
     * @return a percentage from 0 to 100; 0 when nothing has been booked
     */
    public double completionRate() {
        List<PickupRequest> all = pickups.listAll();
        if (all.isEmpty()) {
            return 0;
        }
        long completed = all.stream()
                .filter(request -> request.getStatus() == PickupStatus.COMPLETED)
                .count();
        return completed * PERCENT / all.size();
    }

    /**
     * Average time from booking a pickup to completing it.
     *
     * @return the average over all completed pickups; zero when none are completed
     */
    public Duration averageTurnaround() {
        List<CollectionLog> logs = pickups.getLogs();
        if (logs.isEmpty()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (CollectionLog log : logs) {
            total = total.plus(turnaroundOf(log));
        }
        return total.dividedBy(logs.size());
    }

    private Duration turnaroundOf(CollectionLog log) {
        LocalDateTime bookedAt = pickups.find(log.requestId()).getCreatedAt();
        return Duration.between(bookedAt, log.completedAt());
    }

    /**
     * Total weight actually collected for each material, from the collection logs.
     *
     * @return kilograms per material; every material is listed, with 0 if nothing was collected
     */
    public Map<Material, Double> totalKgByMaterial() {
        Map<Material, Double> totals = new EnumMap<>(Material.class);
        for (Material material : Material.values()) {
            totals.put(material, 0.0);
        }
        for (CollectionLog log : pickups.getLogs()) {
            Material material = pickups.find(log.requestId()).getMaterial();
            totals.merge(material, log.actualWeightKg(), Double::sum);
        }
        return totals;
    }

    /**
     * Counts the pickups in each status, for example how many are still waiting for a collector.
     *
     * @return the number of pickups per status; every status is listed, with 0 if there are none
     */
    public Map<PickupStatus, Long> countByStatus() {
        Map<PickupStatus, Long> counts = new EnumMap<>(PickupStatus.class);
        for (PickupStatus status : PickupStatus.values()) {
            counts.put(status, 0L);
        }
        for (PickupRequest request : pickups.listAll()) {
            counts.merge(request.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    /**
     * Total reward points credited to residents for completed pickups.
     *
     * @return the sum of the points in every collection log; 0 when nothing is completed
     */
    public int pointsAwarded() {
        return pickups.getLogs().stream().mapToInt(CollectionLog::pointsAwarded).sum();
    }

    /**
     * Total weight actually collected from one resident's completed pickups.
     *
     * @param resident the resident
     * @return kilograms recycled by that resident; 0 when none of their pickups is completed
     */
    public double kgRecycledBy(Resident resident) {
        return pickups.getLogs().stream()
                .filter(log -> pickups.find(log.requestId()).getResident().getId().equals(resident.getId()))
                .mapToDouble(CollectionLog::actualWeightKg)
                .sum();
    }
}
