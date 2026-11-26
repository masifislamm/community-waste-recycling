package cwrm.controller;

import cwrm.model.Material;
import cwrm.model.PickupStatus;
import java.time.Duration;
import java.util.Map;

/**
 * The figures shown on the Home and Administrator screens, collected in one place.
 *
 * @param completionRatePercent share of booked pickups that were completed, from 0 to 100
 * @param averageTurnaround     average time from booking to completion
 * @param kgByMaterial          kilograms collected for each material
 * @param pickupsByStatus       number of pickups in each status
 * @param pointsAwarded         reward points credited to residents so far
 */
public record StatisticsSummary(double completionRatePercent, Duration averageTurnaround,
                                Map<Material, Double> kgByMaterial, Map<PickupStatus, Long> pickupsByStatus,
                                int pointsAwarded) {

    /** @return kilograms collected across all materials */
    public double totalKg() {
        return kgByMaterial.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** @return the number of pickups ever booked, in any status */
    public long totalPickups() {
        return pickupsByStatus.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Counts the pickups in one status.
     *
     * @param status the status, for example {@link PickupStatus#PENDING}
     * @return how many pickups have that status
     */
    public long count(PickupStatus status) {
        return pickupsByStatus.getOrDefault(status, 0L);
    }
}
