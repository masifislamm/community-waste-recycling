package cwrm.view;

import cwrm.controller.PickupController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * The main window: a green header and one tab per screen, starting with Home.
 * Opening a tab reloads its data, so changes made on one tab (for example a new
 * booking) show up on the others.
 */
public class MainFrame extends JFrame {
    private static final String TITLE = "CWRM - Community Waste and Recycling Manager";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 820;
    private static final Dimension MINIMUM_SIZE = new Dimension(1000, 640);

    private final JTabbedPane tabs = new JTabbedPane();
    private final List<Refreshable> screens = new ArrayList<>();

    /**
     * Creates the main window with its five screens.
     *
     * @param controller passed to every screen; the screens talk only to it
     */
    public MainFrame(PickupController controller) {
        super(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addScreens(controller);
        tabs.addChangeListener(event -> screens.get(tabs.getSelectedIndex()).refresh());
        JPanel content = new JPanel(new BorderLayout());
        content.add(new HeaderBar(), BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);
        fitToScreen();
    }

    private void addScreens(PickupController controller) {
        BookingPanel booking = new BookingPanel(controller);
        RewardsPanel rewards = new RewardsPanel(controller);
        HomePanel home = new HomePanel(controller, () -> tabs.setSelectedComponent(booking),
                () -> tabs.setSelectedComponent(rewards));
        addScreen("Home", "Start here: what CWRM does and how the pilot is going", home);
        addScreen("Book a pickup", "For residents: book a recycling or bulky-waste pickup", booking);
        addScreen("My rewards", "For residents: points, recycled weight and redeeming points", rewards);
        addScreen("Collector", "For collectors: complete or cancel waiting pickups", new CollectorPanel(controller));
        addScreen("Administrator", "For the council: pilot figures and the reward rule", new AdminPanel(controller));
    }

    private <T extends JComponent & Refreshable> void addScreen(String title, String tip, T screen) {
        tabs.addTab(title, null, screen, tip);
        screens.add(screen);
    }

    /** Uses the normal window size, made smaller if the screen is smaller, and centres the window. */
    private void fitToScreen() {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(WINDOW_WIDTH, screen.width), Math.min(WINDOW_HEIGHT, screen.height));
        setMinimumSize(new Dimension(Math.min(MINIMUM_SIZE.width, screen.width),
                Math.min(MINIMUM_SIZE.height, screen.height)));
        setLocationRelativeTo(null);
    }
}
