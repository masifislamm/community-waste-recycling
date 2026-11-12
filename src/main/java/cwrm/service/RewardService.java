package cwrm.service;

import cwrm.model.Material;

/**
 * Calculates reward points using whichever {@link RewardRule} is currently active.
 *
 * <p>This class never changes when a new rule is added: administrators swap the
 * active rule at runtime with {@link #setRule(RewardRule)}.
 */
public class RewardService {
    private static final String MISSING_RULE = "A reward rule must be chosen.";

    private RewardRule rule;

    /**
     * Creates the service with a starting rule.
     *
     * @param initialRule the rule to use until another one is set
     * @throws IllegalArgumentException if {@code initialRule} is missing
     */
    public RewardService(RewardRule initialRule) {
        setRule(initialRule);
    }

    /**
     * Replaces the active rule. Points calculated from now on use the new rule.
     *
     * @param rule the new rule
     * @throws IllegalArgumentException if {@code rule} is missing
     */
    public void setRule(RewardRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException(MISSING_RULE);
        }
        this.rule = rule;
    }

    /** @return the rule currently in use */
    public RewardRule getRule() {
        return rule;
    }

    /**
     * Calculates points with the active rule.
     *
     * @param material the material collected
     * @param kg       the weight collected in kilograms; 0 or more
     * @return the points earned
     * @throws IllegalArgumentException if {@code kg} is negative
     */
    public int pointsFor(Material material, double kg) {
        return rule.calculatePoints(material, kg);
    }
}
