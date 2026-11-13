package cwrm.controller;

import cwrm.model.Material;
import java.time.Duration;
import java.util.Map;

/**
 * The figures shown to the municipal administrator, collected in one place.
 *
 * @param completionRatePercent share of booked pickups that were completed, from 0 to 100
 * @param averageTurnaround     average time from booking to completion
 * @param kgByMaterial          kilograms collected for each material
 */
public record StatisticsSummary(double completionRatePercent, Duration averageTurnaround,
                                Map<Material, Double> kgByMaterial) {
}
