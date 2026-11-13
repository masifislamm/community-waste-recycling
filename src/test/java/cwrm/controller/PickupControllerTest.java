package cwrm.controller;

import static cwrm.api.FakeHttpTransport.answering;
import static cwrm.api.FakeHttpTransport.status;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cwrm.api.FakeHttpTransport;
import cwrm.api.NominatimAddressLookup;
import cwrm.api.NominatimAddressLookup.LookupResult;
import cwrm.model.Collector;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import cwrm.model.TimeSlot;
import cwrm.service.PerKgRule;
import cwrm.service.PerMaterialRule;
import cwrm.service.PickupService;
import cwrm.service.RewardRule;
import cwrm.service.RewardService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link PickupController}: input validation and the calls it makes to the services. */
class PickupControllerTest {
    private static final String ADDRESS_TEXT = "1 Jalan Ujian, Kuala Lumpur";
    private static final String FOUND_NAME = "Jalan Ujian, Kuala Lumpur, Malaysia";
    private static final String ONE_RESULT = "[{\"lat\":\"3.139\",\"lon\":\"101.6869\","
            + "\"display_name\":\"" + FOUND_NAME + "\"}]";
    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();
    private static final String FIVE_KG = "5";
    private static final int PER_KG_POINTS_FOR_FIVE_KG = 10;
    private static final String FIRST_ID = "PU-001";

    private final Resident resident = new Resident("R-001", "Test Resident");
    private final Collector collector = new Collector("C-001", "Test Collector");
    private final RewardRule perKg = new PerKgRule();
    private final RewardRule perMaterial = new PerMaterialRule();
    private FakeHttpTransport transport;
    private PickupController controller;

    @BeforeEach
    void setUp() {
        transport = answering(status(200, ONE_RESULT));
        RewardService rewards = new RewardService(perKg);
        controller = new PickupController(new PickupService(rewards), rewards,
                new NominatimAddressLookup(transport), List.of(resident), List.of(collector),
                List.of(perKg, perMaterial));
    }

    private BookingForm form(String dateText, String weightText) {
        return new BookingForm(resident, ADDRESS_TEXT, Material.PAPER, dateText, weightText, "", null);
    }

    private String bookOne() {
        controller.bookPickup(form(TOMORROW, ""));
        return controller.allPickups().get(0).getId();
    }

    @Test
    void bookPickup_validInput_returnsSuccess() {
        Outcome outcome = controller.bookPickup(form(TOMORROW, FIVE_KG));

        PickupRequest booked = controller.allPickups().get(0);
        assertAll(
                () -> assertTrue(outcome.success()),
                () -> assertTrue(outcome.message().contains(FIRST_ID)),
                () -> assertEquals(PickupStatus.PENDING, booked.getStatus()),
                () -> assertEquals(5.0, booked.getEstimatedWeightKg().orElseThrow()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "2026-13-40", "2020-01-01"})
    void bookPickup_invalidDateText_returnsFailure(String dateText) {
        Outcome outcome = controller.bookPickup(form(dateText, ""));

        assertFalse(outcome.success());
        assertTrue(controller.allPickups().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-3", "NaN", "5000"})
    void bookPickup_invalidWeightText_returnsFailure(String weightText) {
        Outcome outcome = controller.bookPickup(form(TOMORROW, weightText));

        assertFalse(outcome.success());
        assertTrue(controller.allPickups().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("incompleteForms")
    void bookPickup_missingChoiceOrAddress_returnsFailure(BookingForm incomplete) {
        assertFalse(controller.bookPickup(incomplete).success());
    }

    static Stream<BookingForm> incompleteForms() {
        Resident someone = new Resident("R-009", "Someone");
        return Stream.of(
                new BookingForm(null, ADDRESS_TEXT, Material.PAPER, TOMORROW, "", "", null),
                new BookingForm(someone, "   ", Material.PAPER, TOMORROW, "", "", null),
                new BookingForm(someone, ADDRESS_TEXT, null, TOMORROW, "", "", null));
    }

    @Test
    void bookPickup_optionalFieldsGiven_storesThem() {
        BookingForm full = new BookingForm(resident, ADDRESS_TEXT, Material.GLASS, TOMORROW, "2.5",
                "Gate code 1234", TimeSlot.MORNING);

        controller.bookPickup(full);

        PickupRequest booked = controller.allPickups().get(0);
        assertEquals("Gate code 1234", booked.getNotes());
        assertEquals(TimeSlot.MORNING, booked.getTimeSlot().orElseThrow());
    }

    @Test
    void bookPickup_textMatchesFoundAddress_usesFoundCoordinates() {
        LookupResult found = controller.findAddress(ADDRESS_TEXT);

        controller.bookPickup(new BookingForm(resident, FOUND_NAME, Material.PAPER, TOMORROW, "", "", null));

        assertEquals(found.address(), controller.allPickups().get(0).getAddress());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void findAddress_blankText_returnsMessageWithoutCallingService(String blank) {
        LookupResult result = controller.findAddress(blank);

        assertFalse(result.hasAddress());
        assertTrue(transport.requestedUrls().isEmpty());
    }

    @Test
    void findAddress_knownPlace_returnsAddress() {
        LookupResult result = controller.findAddress(ADDRESS_TEXT);

        assertEquals(FOUND_NAME, result.address().displayName());
    }

    @Test
    void completePickup_validInput_creditsPointsAndReturnsSuccess() {
        String id = bookOne();

        Outcome outcome = controller.completePickup(id, collector, FIVE_KG);

        assertAll(
                () -> assertTrue(outcome.success()),
                () -> assertEquals(PER_KG_POINTS_FOR_FIVE_KG, resident.getAccount().getBalance()),
                () -> assertEquals(1, controller.logs().size()),
                () -> assertTrue(controller.pendingPickups().isEmpty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"heavy", "", "-1"})
    void completePickup_nonNumericWeight_returnsFailure(String weightText) {
        String id = bookOne();

        Outcome outcome = controller.completePickup(id, collector, weightText);

        assertFalse(outcome.success());
        assertEquals(PickupStatus.PENDING, controller.allPickups().get(0).getStatus());
    }

    @Test
    void completePickup_noSelection_returnsFailure() {
        bookOne();

        assertFalse(controller.completePickup(null, collector, FIVE_KG).success());
    }

    @Test
    void completePickup_noCollector_returnsFailure() {
        String id = bookOne();

        assertFalse(controller.completePickup(id, null, FIVE_KG).success());
    }

    @Test
    void completePickup_secondTime_returnsFailureWithoutExtraPoints() {
        String id = bookOne();
        controller.completePickup(id, collector, FIVE_KG);

        Outcome second = controller.completePickup(id, collector, FIVE_KG);

        assertFalse(second.success());
        assertEquals(PER_KG_POINTS_FOR_FIVE_KG, resident.getAccount().getBalance());
    }

    @Test
    void cancelPickup_validSelection_returnsSuccess() {
        String id = bookOne();

        Outcome outcome = controller.cancelPickup(id, collector);

        assertTrue(outcome.success());
        assertEquals(PickupStatus.CANCELLED, controller.pickupsFor(resident).get(0).getStatus());
    }

    @Test
    void cancelPickup_noSelection_returnsFailure() {
        assertFalse(controller.cancelPickup("", collector).success());
    }

    @Test
    void changeRule_otherRule_becomesActiveRule() {
        Outcome outcome = controller.changeRule(perMaterial);

        assertTrue(outcome.success());
        assertSame(perMaterial, controller.activeRule());
    }

    @Test
    void changeRule_noRuleChosen_returnsFailureAndKeepsRule() {
        assertFalse(controller.changeRule(null).success());
        assertSame(perKg, controller.activeRule());
    }

    @Test
    void statistics_oneOfTwoCompleted_reportsRateAndKg() {
        String first = bookOne();
        bookOne();
        controller.completePickup(first, collector, FIVE_KG);

        StatisticsSummary summary = controller.statistics();

        assertEquals(50.0, summary.completionRatePercent());
        assertEquals(5.0, summary.kgByMaterial().get(Material.PAPER));
    }

    @Test
    void lists_givenAtStart_areReturnedUnchanged() {
        assertAll(
                () -> assertEquals(List.of(resident), controller.residents()),
                () -> assertEquals(List.of(collector), controller.collectors()),
                () -> assertEquals(List.of(perKg, perMaterial), controller.ruleOptions()));
    }
}
