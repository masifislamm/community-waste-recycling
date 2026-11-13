package cwrm.service;

/**
 * Input check shared by the reward rules, so each rule rejects bad weights the same way.
 */
final class WeightCheck {
    private static final String INVALID_WEIGHT = "Weight must be a number of 0 kg or more.";

    private WeightCheck() {
        // Holds a static helper only; no instances.
    }

    static void requireNotNegative(double kg) {
        if (kg < 0 || !Double.isFinite(kg)) {
            throw new IllegalArgumentException(INVALID_WEIGHT);
        }
    }
}
