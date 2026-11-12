package cwrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * A resident's request for a recycling or bulky-waste pickup.
 *
 * <p>Requests are created only through {@link Builder}, which checks the five
 * mandatory fields and the optional weight in one place. A new request starts as
 * {@link PickupStatus#PENDING} and can then be completed or cancelled exactly once.
 */
public class PickupRequest {
    private static final String NO_NOTES = "";
    private static final String CANNOT_COMPLETE = "Pickup %s cannot be completed because its status is %s.";
    private static final String CANNOT_CANCEL = "Pickup %s cannot be cancelled because its status is %s.";

    private final String id;
    private final Resident resident;
    private final Address address;
    private final Material material;
    private final LocalDate date;
    private final Double estimatedWeightKg;
    private final String notes;
    private final TimeSlot timeSlot;
    private final LocalDateTime createdAt;
    private PickupStatus status;

    private PickupRequest(Builder builder) {
        this.id = builder.id;
        this.resident = builder.resident;
        this.address = builder.address;
        this.material = builder.material;
        this.date = builder.date;
        this.estimatedWeightKg = builder.estimatedWeightKg;
        this.notes = Objects.requireNonNullElse(builder.notes, NO_NOTES);
        this.timeSlot = builder.timeSlot;
        this.createdAt = LocalDateTime.now();
        this.status = PickupStatus.PENDING;
    }

    /**
     * Marks this pickup as completed.
     *
     * @throws IllegalStateException if the pickup is not pending
     */
    public void complete() {
        requirePending(CANNOT_COMPLETE);
        status = PickupStatus.COMPLETED;
    }

    /**
     * Marks this pickup as cancelled.
     *
     * @throws IllegalStateException if the pickup is not pending
     */
    public void cancel() {
        requirePending(CANNOT_CANCEL);
        status = PickupStatus.CANCELLED;
    }

    private void requirePending(String messageTemplate) {
        if (status != PickupStatus.PENDING) {
            throw new IllegalStateException(String.format(messageTemplate, id, status));
        }
    }

    /** @return the unique request ID, for example {@code PU-001} */
    public String getId() {
        return id;
    }

    /** @return the resident who booked the pickup */
    public Resident getResident() {
        return resident;
    }

    /** @return where the waste should be collected */
    public Address getAddress() {
        return address;
    }

    /** @return the kind of waste to collect */
    public Material getMaterial() {
        return material;
    }

    /** @return the requested pickup date */
    public LocalDate getDate() {
        return date;
    }

    /** @return the resident's weight estimate in kilograms, or empty if none was given */
    public Optional<Double> getEstimatedWeightKg() {
        return Optional.ofNullable(estimatedWeightKg);
    }

    /** @return notes for the collector, or an empty string if none were given */
    public String getNotes() {
        return notes;
    }

    /** @return the preferred time slot, or empty if any time is fine */
    public Optional<TimeSlot> getTimeSlot() {
        return Optional.ofNullable(timeSlot);
    }

    /** @return the current status of the request */
    public PickupStatus getStatus() {
        return status;
    }

    /** @return when the request was created; used to measure turnaround time */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Builds a {@link PickupRequest} one field at a time.
     *
     * <p>Mandatory: id, resident, address, material and date.
     * Optional: estimated weight, notes and time slot.
     * {@link #build()} checks every field before the request is created.
     */
    public static class Builder {
        private static final String MISSING_ID = "A request ID is required.";
        private static final String MISSING_RESIDENT = "Please choose a resident.";
        private static final String MISSING_ADDRESS = "Please enter an address.";
        private static final String MISSING_MATERIAL = "Please choose a material.";
        private static final String MISSING_DATE = "Please choose a pickup date.";
        private static final String INVALID_WEIGHT = "Estimated weight must be a number greater than 0 kg.";

        private String id;
        private Resident resident;
        private Address address;
        private Material material;
        private LocalDate date;
        private Double estimatedWeightKg;
        private String notes;
        private TimeSlot timeSlot;

        /** Creates an empty builder; set the fields with the methods below. */
        public Builder() {
            // All fields start empty; build() reports any mandatory field that is still missing.
        }

        /**
         * Sets the request ID (mandatory).
         *
         * @param id unique request ID, for example {@code PU-001}
         * @return this builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the resident who books the pickup (mandatory).
         *
         * @param resident the resident
         * @return this builder
         */
        public Builder resident(Resident resident) {
            this.resident = resident;
            return this;
        }

        /**
         * Sets the pickup address (mandatory).
         *
         * @param address the address
         * @return this builder
         */
        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the kind of waste (mandatory).
         *
         * @param material the material
         * @return this builder
         */
        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        /**
         * Sets the pickup date (mandatory).
         *
         * @param date the date
         * @return this builder
         */
        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the resident's weight estimate (optional).
         *
         * @param estimatedWeightKg estimated weight in kilograms; must be greater than 0
         * @return this builder
         */
        public Builder estimatedWeightKg(double estimatedWeightKg) {
            this.estimatedWeightKg = estimatedWeightKg;
            return this;
        }

        /**
         * Sets notes for the collector (optional).
         *
         * @param notes free text, for example a gate code
         * @return this builder
         */
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * Sets the preferred time slot (optional).
         *
         * @param timeSlot the time slot
         * @return this builder
         */
        public Builder timeSlot(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            return this;
        }

        /**
         * Checks every field and creates the request.
         *
         * @return a new request with status {@link PickupStatus#PENDING}
         * @throws IllegalStateException if a mandatory field is missing or the weight is not positive
         */
        public PickupRequest build() {
            requireText(id, MISSING_ID);
            requirePresent(resident, MISSING_RESIDENT);
            requirePresent(address, MISSING_ADDRESS);
            requirePresent(material, MISSING_MATERIAL);
            requirePresent(date, MISSING_DATE);
            requirePositiveWeight();
            return new PickupRequest(this);
        }

        private static void requirePresent(Object value, String message) {
            if (value == null) {
                throw new IllegalStateException(message);
            }
        }

        private static void requireText(String value, String message) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException(message);
            }
        }

        private void requirePositiveWeight() {
            if (estimatedWeightKg == null) {
                return; // optional field that was not given
            }
            if (!Double.isFinite(estimatedWeightKg) || estimatedWeightKg <= 0) {
                throw new IllegalStateException(INVALID_WEIGHT);
            }
        }
    }
}
