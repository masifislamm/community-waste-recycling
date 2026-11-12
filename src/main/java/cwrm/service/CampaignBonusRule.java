package cwrm.service;

import cwrm.model.Material;

/**
 * Reward rule for a recycling campaign: it multiplies the points of one target
 * material and leaves every other material unchanged.
 *
 * <p>It wraps another rule (the base rule), which calculates the normal points first.
 */
public class CampaignBonusRule implements RewardRule {
    private static final int MIN_MULTIPLIER = 1;
    private static final String NAME_FORMAT = "%s + %dx %s campaign";
    private static final String MISSING_ARGUMENT = "A campaign needs a base rule and a target material.";
    private static final String INVALID_MULTIPLIER = "The campaign multiplier must be at least 1.";

    private final RewardRule base;
    private final Material target;
    private final int multiplier;

    /**
     * Creates a campaign rule.
     *
     * @param base       the rule that calculates the normal points
     * @param target     the material that earns bonus points
     * @param multiplier how many times the normal points the target material earns; at least 1
     * @throws IllegalArgumentException if {@code base} or {@code target} is missing,
     *                                  or {@code multiplier} is below 1
     */
    public CampaignBonusRule(RewardRule base, Material target, int multiplier) {
        if (base == null || target == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENT);
        }
        if (multiplier < MIN_MULTIPLIER) {
            throw new IllegalArgumentException(INVALID_MULTIPLIER);
        }
        this.base = base;
        this.target = target;
        this.multiplier = multiplier;
    }

    @Override
    public int calculatePoints(Material material, double kg) {
        WeightCheck.requireNotNegative(kg);
        int basePoints = base.calculatePoints(material, kg);
        return material == target ? basePoints * multiplier : basePoints;
    }

    @Override
    public String getName() {
        return String.format(NAME_FORMAT, base.getName(), multiplier, target.getLabel());
    }
}
