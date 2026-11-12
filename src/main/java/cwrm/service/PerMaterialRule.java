package cwrm.service;

import cwrm.model.Material;

/**
 * Reward rule that uses each material's own rate, so harder-to-recycle materials
 * such as e-waste earn more points per kilogram.
 */
public class PerMaterialRule implements RewardRule {
    private static final String NAME = "Rate per material";

    @Override
    public int calculatePoints(Material material, double kg) {
        WeightCheck.requireNotNegative(kg);
        return (int) (kg * material.getPointsPerKg());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
