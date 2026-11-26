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
import cwrm.view.Theme;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Starts the app: creates the services, the fictional seed data (see {@link DemoData})
 * and the controller, then opens the main window on the Swing event thread.
 *
 * <p>All data is held in memory.
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
            Theme.install();
            new MainFrame(controller).setVisible(true);
        });
    }

    private static PickupController createController() {
        RewardRule perMaterial = new PerMaterialRule();
        List<RewardRule> ruleOptions = List.of(perMaterial, new PerKgRule(),
                new CampaignBonusRule(perMaterial, Material.PLASTIC, CAMPAIGN_MULTIPLIER));
        RewardService rewards = new RewardService(perMaterial);
        PickupService pickups = new PickupService(rewards);
        List<Resident> residents = DemoData.residents();
        List<Collector> collectors = DemoData.collectors();
        DemoData.addSamplePickups(pickups, residents, collectors);
        NominatimAddressLookup addressLookup = new NominatimAddressLookup(new JdkHttpTransport());
        return new PickupController(pickups, rewards, addressLookup, residents, collectors, ruleOptions);
    }
}
