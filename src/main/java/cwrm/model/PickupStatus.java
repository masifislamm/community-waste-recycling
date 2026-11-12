package cwrm.model;

/**
 * Where a pickup request is in its life cycle.
 *
 * <p>Every request starts as {@code PENDING} and moves once, to either
 * {@code COMPLETED} or {@code CANCELLED}.
 */
public enum PickupStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
