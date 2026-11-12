package cwrm.service;

import static cwrm.model.TestData.newResident;
import static cwrm.model.TestData.validBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cwrm.model.Collector;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link StatisticsService}: completion rate, turnaround and kilograms per material. */
class StatisticsServiceTest {
    private static final double FIVE_KG = 5.0;
    private static final double THREE_KG = 3.0;
    private static final double TWO_KG = 2.0;
    private static final double FIFTY_PERCENT = 50.0;
    private static final double DELTA = 0.0001;

    private final Collector collector = new Collector("C-001", "Test Collector");
    private PickupService pickups;
    private StatisticsService statistics;

    @BeforeEach
    void setUp() {
        pickups = new PickupService(new RewardService(new PerKgRule()));
        statistics = new StatisticsService(pickups);
    }

    private String book(Material material) {
        PickupRequest request = validBuilder()
                .id(pickups.nextRequestId())
                .resident(newResident())
                .material(material)
                .build();
        pickups.book(request);
        return request.getId();
    }

    @Test
    void completionRate_noRequests_returnsZero() {
        assertEquals(0, statistics.completionRate(), DELTA);
    }

    @Test
    void completionRate_oneOfTwoCompleted_returnsFifty() {
        String completed = book(Material.PAPER);
        book(Material.PAPER);
        pickups.complete(completed, collector, FIVE_KG);

        assertEquals(FIFTY_PERCENT, statistics.completionRate(), DELTA);
    }

    @Test
    void completionRate_cancelledPickup_countsAsNotCompleted() {
        String completed = book(Material.PAPER);
        String cancelled = book(Material.PAPER);
        pickups.complete(completed, collector, FIVE_KG);
        pickups.cancel(cancelled);

        assertEquals(FIFTY_PERCENT, statistics.completionRate(), DELTA);
    }

    @Test
    void totalKgByMaterial_sumsCompletedLogs() {
        pickups.complete(book(Material.PAPER), collector, FIVE_KG);
        pickups.complete(book(Material.PAPER), collector, THREE_KG);
        pickups.complete(book(Material.PLASTIC), collector, TWO_KG);
        book(Material.GLASS);

        Map<Material, Double> totals = statistics.totalKgByMaterial();

        assertEquals(FIVE_KG + THREE_KG, totals.get(Material.PAPER), DELTA);
        assertEquals(TWO_KG, totals.get(Material.PLASTIC), DELTA);
        assertEquals(0, totals.get(Material.GLASS), DELTA);
    }

    @Test
    void totalKgByMaterial_noLogs_listsEveryMaterialWithZero() {
        Map<Material, Double> totals = statistics.totalKgByMaterial();

        assertEquals(Material.values().length, totals.size());
        totals.values().forEach(kg -> assertEquals(0, kg, DELTA));
    }

    @Test
    void averageTurnaround_noCompletedPickups_returnsZero() {
        book(Material.PAPER);

        assertEquals(Duration.ZERO, statistics.averageTurnaround());
    }

    @Test
    void averageTurnaround_completedPickup_isNotNegative() {
        pickups.complete(book(Material.PAPER), collector, FIVE_KG);

        assertFalse(statistics.averageTurnaround().isNegative());
    }
}
