package cwrm;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cwrm.model.Collector;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import cwrm.service.PerMaterialRule;
import cwrm.service.PickupService;
import cwrm.service.RewardService;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link DemoData}: the seed data the app starts with. */
class DemoDataTest {

    private static long countWith(List<PickupRequest> pickups, PickupStatus status) {
        return pickups.stream().filter(request -> request.getStatus() == status).count();
    }

    @Test
    void addSamplePickups_freshService_booksSevenPickupsInEveryStatus() {
        PickupService pickups = new PickupService(new RewardService(new PerMaterialRule()));
        List<Resident> residents = DemoData.residents();
        List<Collector> collectors = DemoData.collectors();

        DemoData.addSamplePickups(pickups, residents, collectors);

        List<PickupRequest> all = pickups.listAll();
        assertAll(
                () -> assertEquals(7, all.size()),
                () -> assertEquals(3, countWith(all, PickupStatus.COMPLETED)),
                () -> assertEquals(1, countWith(all, PickupStatus.CANCELLED)),
                () -> assertEquals(3, countWith(all, PickupStatus.PENDING)),
                () -> assertEquals(3, pickups.getLogs().size()),
                () -> assertEquals("PU-008", pickups.nextRequestId()));
    }

    @Test
    void addSamplePickups_completedPaper_creditsResidentWithPerMaterialPoints() {
        PickupService pickups = new PickupService(new RewardService(new PerMaterialRule()));
        List<Resident> residents = DemoData.residents();

        DemoData.addSamplePickups(pickups, residents, DemoData.collectors());

        int paperPointsForSixAndAHalfKg = 13;
        assertEquals(paperPointsForSixAndAHalfKg, residents.get(0).getAccount().getBalance());
    }

    @Test
    void residentsAndCollectors_seedData_matchTheIntegrationTestDocument() {
        assertAll(
                () -> assertEquals(3, DemoData.residents().size()),
                () -> assertEquals("Aisyah Contoh", DemoData.residents().get(0).getName()),
                () -> assertEquals(2, DemoData.collectors().size()));
    }
}
