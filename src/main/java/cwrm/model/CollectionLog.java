package cwrm.model;

import java.time.LocalDateTime;

/**
 * A record written when a collector completes a pickup: who collected it,
 * the weight actually collected and the points the resident received.
 *
 * @param requestId      ID of the completed pickup request
 * @param collector      collector who completed the pickup
 * @param actualWeightKg weight collected, in kilograms
 * @param pointsAwarded  reward points credited to the resident
 * @param completedAt    date and time the pickup was completed
 */
public record CollectionLog(String requestId, Collector collector, double actualWeightKg,
                            int pointsAwarded, LocalDateTime completedAt) {
}
