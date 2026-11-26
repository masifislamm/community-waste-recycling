package cwrm;

import cwrm.model.Address;
import cwrm.model.Collector;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import cwrm.model.Resident;
import cwrm.model.TimeSlot;
import cwrm.service.PickupService;
import java.time.LocalDate;
import java.util.List;

/**
 * The fictional people and sample pickups the app starts with, so every screen has
 * something to show in a demo. All names and addresses are made up; data is kept in memory only.
 */
final class DemoData {
    private static final double NO_COORDINATE = 0.0;
    private static final String BANGSAR = "12 Jalan Contoh 3, Bangsar, Kuala Lumpur";
    private static final String CHERAS = "8 Lorong Sampel 2, Cheras, Kuala Lumpur";
    private static final String SETAPAK = "21 Jalan Demo 7, Setapak, Kuala Lumpur";

    /**
     * One sample pickup and what has happened to it.
     *
     * @param resident position of the resident in {@link #residents()}
     * @param address  the (fictional) address
     * @param material what is collected
     * @param daysAhead how many days from today the pickup is booked for
     * @param slot     the preferred time slot, or {@code null} for any time
     * @param kg       the estimated weight, also used as the collected weight
     * @param result   the status the sample should end in
     */
    private record Sample(int resident, String address, Material material, int daysAhead, TimeSlot slot,
                          double kg, PickupStatus result) {
    }

    private static final List<Sample> SAMPLES = List.of(
            new Sample(0, BANGSAR, Material.PAPER, 1, TimeSlot.MORNING, 6.5, PickupStatus.COMPLETED),
            new Sample(1, CHERAS, Material.PLASTIC, 1, TimeSlot.AFTERNOON, 3.0, PickupStatus.COMPLETED),
            new Sample(2, SETAPAK, Material.E_WASTE, 2, null, 4.0, PickupStatus.COMPLETED),
            new Sample(0, BANGSAR, Material.BULKY, 3, TimeSlot.MORNING, 25.0, PickupStatus.CANCELLED),
            new Sample(1, CHERAS, Material.GLASS, 2, TimeSlot.EVENING, 5.0, PickupStatus.PENDING),
            new Sample(2, SETAPAK, Material.METAL, 3, null, 2.5, PickupStatus.PENDING),
            new Sample(0, BANGSAR, Material.PAPER, 4, TimeSlot.AFTERNOON, 8.0, PickupStatus.PENDING));

    private DemoData() {
        // Static data only; no instances.
    }

    /** @return three fictional residents */
    static List<Resident> residents() {
        return List.of(
                new Resident("R-001", "Aisyah Contoh"),
                new Resident("R-002", "Daniel Sample"),
                new Resident("R-003", "Mei Ling Demo"));
    }

    /** @return two fictional collectors */
    static List<Collector> collectors() {
        return List.of(
                new Collector("C-001", "Collector Hafiz (demo)"),
                new Collector("C-002", "Collector Priya (demo)"));
    }

    /**
     * Books the sample pickups through the normal service, then completes or cancels some
     * of them, so points, logs and statistics are all filled in the usual way.
     *
     * @param pickups    the service to book them with
     * @param residents  the residents from {@link #residents()}
     * @param collectors the collectors from {@link #collectors()}
     */
    static void addSamplePickups(PickupService pickups, List<Resident> residents, List<Collector> collectors) {
        for (int index = 0; index < SAMPLES.size(); index++) {
            Sample sample = SAMPLES.get(index);
            PickupRequest request = new PickupRequest.Builder()
                    .id(pickups.nextRequestId())
                    .resident(residents.get(sample.resident()))
                    .address(new Address(sample.address(), NO_COORDINATE, NO_COORDINATE))
                    .material(sample.material())
                    .date(LocalDate.now().plusDays(sample.daysAhead()))
                    .estimatedWeightKg(sample.kg())
                    .timeSlot(sample.slot())
                    .build();
            pickups.book(request);
            finish(pickups, request.getId(), sample, collectors.get(index % collectors.size()));
        }
    }

    private static void finish(PickupService pickups, String id, Sample sample, Collector collector) {
        if (sample.result() == PickupStatus.COMPLETED) {
            pickups.complete(id, collector, sample.kg());
        } else if (sample.result() == PickupStatus.CANCELLED) {
            pickups.cancel(id);
        }
    }
}
