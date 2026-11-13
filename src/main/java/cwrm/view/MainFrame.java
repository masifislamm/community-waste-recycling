package cwrm.view;

import cwrm.controller.PickupController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * The main window: one tab per user role. Opening a tab reloads its data,
 * so changes made on one tab (for example a new booking) show up on the others.
 */
public class MainFrame extends JFrame {
    private static final String TITLE = "CWRM - Community Waste and Recycling Manager";
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 720;

    private final JTabbedPane tabs = new JTabbedPane();
    private final List<Refreshable> screens = new ArrayList<>();

    /**
     * Creates the main window with its four screens.
     *
     * @param controller passed to every screen; the screens talk only to it
     */
    public MainFrame(PickupController controller) {
        super(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addScreen("Book pickup", new BookingPanel(controller));
        addScreen("Collector", new CollectorPanel(controller));
        addScreen("My rewards", new RewardsPanel(controller));
        addScreen("Administrator", new AdminPanel(controller));
        tabs.addChangeListener(event -> screens.get(tabs.getSelectedIndex()).refresh());
        setContentPane(tabs);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    private <T extends JComponent & Refreshable> void addScreen(String title, T screen) {
        tabs.addTab(title, screen);
        screens.add(screen);
    }
}
