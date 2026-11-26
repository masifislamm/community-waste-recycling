package cwrm.model;

/**
 * Part of the day a resident prefers for their pickup. Choosing one is optional.
 * The hours shown are the pilot's planned collection windows.
 */
public enum TimeSlot {
    MORNING("Morning (8 am - 12 pm)"),
    AFTERNOON("Afternoon (12 pm - 4 pm)"),
    EVENING("Evening (4 pm - 7 pm)");

    private final String label;

    TimeSlot(String label) {
        this.label = label;
    }

    /** @return the name and hours shown to users, for example {@code Morning (8 am - 12 pm)} */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
