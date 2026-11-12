package cwrm.model;

/**
 * The kinds of waste a resident can book a pickup for.
 *
 * <p>Each material has a label for display and a reward rate in points per kilogram,
 * used by the per-material reward rule.
 */
public enum Material {
    PAPER("Paper", Rates.PAPER),
    PLASTIC("Plastic", Rates.PLASTIC),
    GLASS("Glass", Rates.GLASS),
    METAL("Metal", Rates.METAL),
    E_WASTE("E-waste", Rates.E_WASTE),
    BULKY("Bulky waste", Rates.BULKY);

    private final String label;
    private final int pointsPerKg;

    Material(String label, int pointsPerKg) {
        this.label = label;
        this.pointsPerKg = pointsPerKg;
    }

    /** @return the name shown to users, for example {@code E-waste} */
    public String getLabel() {
        return label;
    }

    /** @return reward points earned for each kilogram of this material */
    public int getPointsPerKg() {
        return pointsPerKg;
    }

    @Override
    public String toString() {
        return label;
    }

    /** Reward rates in points per kilogram, kept together as named constants. */
    private static final class Rates {
        private static final int PAPER = 2;
        private static final int PLASTIC = 3;
        private static final int GLASS = 2;
        private static final int METAL = 4;
        private static final int E_WASTE = 5;
        private static final int BULKY = 1;
    }
}
