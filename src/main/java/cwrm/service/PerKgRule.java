package cwrm.service;

import cwrm.model.Material;

/**
 * Reward rule that gives the same points for every kilogram, whatever the material.
 */
public class PerKgRule implements RewardRule {
    private static final int POINTS_PER_KG = 2;
    private static final String NAME = "Flat rate per kg";

    @Override
    public int calculatePoints(Material material, double kg) {
        WeightCheck.requireNotNegative(kg);
        // The int cast drops any part of a point (see the known-limitation test).
        return (int) (kg * POINTS_PER_KG);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
