package cwrm.model;

import static cwrm.model.TestData.ADDRESS;
import static cwrm.model.TestData.PICKUP_DATE;
import static cwrm.model.TestData.REQUEST_ID;
import static cwrm.model.TestData.RESIDENT_ID;
import static cwrm.model.TestData.validBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link PickupRequest.Builder}: mandatory fields, optional fields and validation. */
class PickupRequestBuilderTest {
    private static final double WEIGHT_KG = 5.0;
    private static final String NOTES = "Gate code 1234";

    @Test
    void build_allMandatoryFields_createsPendingRequest() {
        PickupRequest request = validBuilder().build();

        assertAll(
                () -> assertEquals(REQUEST_ID, request.getId()),
                () -> assertEquals(RESIDENT_ID, request.getResident().getId()),
                () -> assertEquals(ADDRESS, request.getAddress()),
                () -> assertEquals(Material.PAPER, request.getMaterial()),
                () -> assertEquals(PICKUP_DATE, request.getDate()),
                () -> assertNotNull(request.getCreatedAt()),
                () -> assertEquals(PickupStatus.PENDING, request.getStatus()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void build_missingOrBlankId_throwsIllegalState(String id) {
        PickupRequest.Builder builder = validBuilder().id(id);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_missingResident_throwsIllegalState() {
        PickupRequest.Builder builder = validBuilder().resident(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_missingAddress_throwsIllegalState() {
        PickupRequest.Builder builder = validBuilder().address(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_missingMaterial_throwsIllegalState() {
        PickupRequest.Builder builder = validBuilder().material(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_missingDate_throwsIllegalState() {
        PickupRequest.Builder builder = validBuilder().date(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, -5.5})
    void build_nonPositiveWeight_throwsIllegalState(double weightKg) {
        PickupRequest.Builder builder = validBuilder().estimatedWeightKg(weightKg);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_optionalFieldsOmitted_usesDefaults() {
        PickupRequest request = validBuilder().build();

        assertAll(
                () -> assertTrue(request.getEstimatedWeightKg().isEmpty()),
                () -> assertEquals("", request.getNotes()),
                () -> assertTrue(request.getTimeSlot().isEmpty()));
    }

    @Test
    void build_optionalFieldsGiven_keepsTheirValues() {
        PickupRequest request = validBuilder()
                .estimatedWeightKg(WEIGHT_KG)
                .notes(NOTES)
                .timeSlot(TimeSlot.MORNING)
                .build();

        assertAll(
                () -> assertEquals(Optional.of(WEIGHT_KG), request.getEstimatedWeightKg()),
                () -> assertEquals(NOTES, request.getNotes()),
                () -> assertEquals(Optional.of(TimeSlot.MORNING), request.getTimeSlot()));
    }
}
