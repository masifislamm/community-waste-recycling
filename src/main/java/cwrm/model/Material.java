package cwrm.model;

/**
 * The kinds of waste a resident can book a pickup for.
 *
 * <p>Each material has a label for display, a short list of examples that tells residents
 * what belongs in it, and a reward rate in points per kilogram, used by the per-material
 * reward rule.
 */
public enum Material {
    PAPER("Paper", "Newspapers, cardboard, magazines, office paper", Rates.PAPER),
    PLASTIC("Plastic", "Bottles, food containers, clean plastic bags", Rates.PLASTIC),
    GLASS("Glass", "Bottles and jars (no broken window glass)", Rates.GLASS),
    METAL("Metal", "Drink cans, food tins, clean aluminium foil", Rates.METAL),
    E_WASTE("E-waste", "Old phones, chargers, cables, small appliances", Rates.E_WASTE),
    BULKY("Bulky waste", "Furniture, mattresses, large appliances", Rates.BULKY);

    private final String label;
    private final String examples;
    private final int pointsPerKg;

    Material(String label, String examples, int pointsPerKg) {
        this.label = label;
        this.examples = examples;
        this.pointsPerKg = pointsPerKg;
    }

    /** @return the name shown to users, for example {@code E-waste} */
    public String getLabel() {
        return label;
    }

    /** @return examples of items that belong to this material, to help residents choose */
    public String getExamples() {
        return examples;
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
