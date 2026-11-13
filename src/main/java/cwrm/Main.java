package cwrm;

import cwrm.api.JdkHttpTransport;
import cwrm.api.NominatimAddressLookup;
import cwrm.controller.PickupController;
import cwrm.model.Collector;
import cwrm.model.Material;
import cwrm.model.Resident;
import cwrm.service.CampaignBonusRule;
import cwrm.service.PerKgRule;
import cwrm.service.PerMaterialRule;
import cwrm.service.PickupService;
import cwrm.service.RewardRule;
import cwrm.service.RewardService;
import cwrm.view.MainFrame;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Starts the app: creates the seed data, the services and the controller,
 * then opens the main window on the Swing event thread.
 *
 * <p>All data is held in memory and the names below are fictional.
 */
public final class Main {
    private static final int CAMPAIGN_MULTIPLIER = 3;

    private Main() {
        // Entry point only; no instances.
    }

    /**
     * Launches the app.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        PickupController controller = createController();
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new MainFrame(controller).setVisible(true);
        });
    }

    private static PickupController createController() {
        RewardRule perMaterial = new PerMaterialRule();
        List<RewardRule> ruleOptions = List.of(perMaterial, new PerKgRule(),
                new CampaignBonusRule(perMaterial, Material.PLASTIC, CAMPAIGN_MULTIPLIER));
        RewardService rewards = new RewardService(perMaterial);
        NominatimAddressLookup addressLookup = new NominatimAddressLookup(new JdkHttpTransport());
        return new PickupController(new PickupService(rewards), rewards, addressLookup,
                seedResidents(), seedCollectors(), ruleOptions);
    }

    private static List<Resident> seedResidents() {
        return List.of(
                new Resident("R-001", "Aisyah Contoh"),
                new Resident("R-002", "Daniel Sample"),
                new Resident("R-003", "Mei Ling Demo"));
    }

    private static List<Collector> seedCollectors() {
        return List.of(
                new Collector("C-001", "Collector Hafiz (demo)"),
                new Collector("C-002", "Collector Priya (demo)"));
    }

    /** Uses the operating system's look so the window looks native; keeps Swing's default if that fails. */
    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException unavailable) {
            // Swing's default look still works; nothing else to do.
        }
    }
}
