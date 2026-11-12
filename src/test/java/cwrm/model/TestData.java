package cwrm.model;

import java.time.LocalDate;

/** Shared, clearly fictional test data for model and service tests. */
public final class TestData {
    public static final String REQUEST_ID = "PU-001";
    public static final String RESIDENT_ID = "R-001";
    public static final String RESIDENT_NAME = "Test Resident";
    public static final Address ADDRESS = new Address("1 Jalan Ujian, Kuala Lumpur", 3.1390, 101.6869);
    public static final LocalDate PICKUP_DATE = LocalDate.of(2026, 12, 1);

    private TestData() {
    }

    /** @return a new resident with an empty reward account */
    public static Resident newResident() {
        return new Resident(RESIDENT_ID, RESIDENT_NAME);
    }

    /** @return a builder with all five mandatory fields set and no optional fields */
    public static PickupRequest.Builder validBuilder() {
        return new PickupRequest.Builder()
                .id(REQUEST_ID)
                .resident(newResident())
                .address(ADDRESS)
                .material(Material.PAPER)
                .date(PICKUP_DATE);
    }
}
