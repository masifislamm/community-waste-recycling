package cwrm.service;

import cwrm.model.Material;

/**
 * Strategy for turning a collected weight into reward points.
 *
 * <p>Each way of rewarding residents is its own class that implements this interface.
 * A new rule is added as a new class, without changing {@link RewardService}
 * (Open/Closed principle), and the active rule can be swapped while the app runs.
 */
public interface RewardRule {

    /**
     * Calculates the points earned for a collection.
     *
     * @param material the material collected
     * @param kg       the weight collected in kilograms; 0 or more
     * @return the points earned
     * @throws IllegalArgumentException if {@code kg} is negative
     */
    int calculatePoints(Material material, double kg);

    /** @return a short name for this rule, shown to administrators */
    String getName();
}
