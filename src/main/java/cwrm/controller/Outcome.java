package cwrm.controller;

/**
 * The result of a user action: whether it worked and a friendly message to show.
 *
 * <p>The controller returns an {@code Outcome} instead of throwing, so the views
 * never see an exception or a stack trace.
 *
 * @param success {@code true} if the action worked
 * @param message a short, user-friendly message
 */
public record Outcome(boolean success, String message) {

    /**
     * Creates a successful outcome.
     *
     * @param message what happened, for example {@code "Pickup PU-001 booked."}
     * @return a successful outcome
     */
    public static Outcome ok(String message) {
        return new Outcome(true, message);
    }

    /**
     * Creates a failed outcome.
     *
     * @param message what went wrong and how to fix it
     * @return a failed outcome
     */
    public static Outcome fail(String message) {
        return new Outcome(false, message);
    }
}
