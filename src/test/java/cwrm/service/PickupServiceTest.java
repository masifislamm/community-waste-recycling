package cwrm.service;

import static cwrm.model.TestData.newResident;
import static cwrm.model.TestData.validBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cwrm.model.CollectionLog;
import cwrm.model.Collector;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link PickupService}: booking, finding, cancelling and completing pickups. */
class PickupServiceTest {
    private static final double FIVE_KG = 5.0;
    private static final int PER_KG_POINTS_FOR_FIVE_KG = 10;
    private static final String UNKNOWN_ID = "PU-999";

    private final Collector collector = new Collector("C-001", "Test Collector");
    private PickupService service;
    private Resident resident;

    @BeforeEach
    void setUp() {
        service = new PickupService(new RewardService(new PerKgRule()));
        resident = newResident();
    }

    private PickupRequest bookFor(Resident owner) {
        PickupRequest request = validBuilder()
                .id(service.nextRequestId())
                .resident(owner)
                .material(Material.PAPER)
                .build();
        service.book(request);
        return request;
    }

    @Test
    void book_validRequest_canBeFound() {
        PickupRequest request = bookFor(resident);

        assertSame(request, service.find(request.getId()));
    }

    @Test
    void book_duplicateId_throwsIllegalArgument() {
        PickupRequest request = bookFor(resident);

        assertThrows(IllegalArgumentException.class, () -> service.book(request));
    }

    @Test
    void find_unknownId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.find(UNKNOWN_ID));
    }

    @Test
    void cancel_pendingPickup_setsCancelled() {
        PickupRequest request = bookFor(resident);

        service.cancel(request.getId());

        assertEquals(PickupStatus.CANCELLED, request.getStatus());
    }

    @Test
    void complete_validPickup_logsAndCreditsPoints() {
        PickupRequest request = bookFor(resident);

        CollectionLog log = service.complete(request.getId(), collector, FIVE_KG);

        assertAll(
                () -> assertEquals(PickupStatus.COMPLETED, request.getStatus()),
                () -> assertEquals(request.getId(), log.requestId()),
                () -> assertSame(collector, log.collector()),
                () -> assertEquals(FIVE_KG, log.actualWeightKg()),
                () -> assertEquals(PER_KG_POINTS_FOR_FIVE_KG, log.pointsAwarded()),
                () -> assertEquals(PER_KG_POINTS_FOR_FIVE_KG, resident.getAccount().getBalance()),
                () -> assertEquals(List.of(log), service.getLogs()));
    }

    @Test
    void complete_calledTwice_doesNotCreditTwice() {
        PickupRequest request = bookFor(resident);
        service.complete(request.getId(), collector, FIVE_KG);

        assertThrows(IllegalStateException.class, () -> service.complete(request.getId(), collector, FIVE_KG));
        assertEquals(PER_KG_POINTS_FOR_FIVE_KG, resident.getAccount().getBalance());
        assertEquals(1, service.getLogs().size());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -2.5})
    void complete_nonPositiveWeight_throwsAndLeavesPickupPending(double kg) {
        PickupRequest request = bookFor(resident);

        assertThrows(IllegalArgumentException.class, () -> service.complete(request.getId(), collector, kg));
        assertEquals(PickupStatus.PENDING, request.getStatus());
    }

    @Test
    void complete_missingCollector_throwsIllegalArgument() {
        PickupRequest request = bookFor(resident);

        assertThrows(IllegalArgumentException.class, () -> service.complete(request.getId(), null, FIVE_KG));
    }

    @Test
    void listFor_resident_returnsOnlyTheirRequests() {
        Resident otherResident = new Resident("R-002", "Other Resident");
        PickupRequest mine = bookFor(resident);
        bookFor(otherResident);

        assertEquals(List.of(mine), service.listFor(resident));
    }

    @Test
    void listAll_twoBookings_returnsBothInBookingOrder() {
        PickupRequest first = bookFor(resident);
        PickupRequest second = bookFor(resident);

        assertEquals(List.of(first, second), service.listAll());
    }

    @Test
    void nextRequestId_calledTwice_returnsSequentialIds() {
        assertEquals("PU-001", service.nextRequestId());
        assertEquals("PU-002", service.nextRequestId());
    }

    @Test
    void getLogs_returnedList_cannotBeChangedFromOutside() {
        List<CollectionLog> logs = service.getLogs();

        assertThrows(UnsupportedOperationException.class, logs::clear);
    }
}
