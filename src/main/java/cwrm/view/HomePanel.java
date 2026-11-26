package cwrm.view;

import cwrm.controller.PickupController;
import cwrm.controller.StatisticsSummary;
import cwrm.model.PickupRequest;
import cwrm.model.PickupStatus;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The first screen users see. It explains what CWRM does in three steps, shows live
 * figures for the pilot and lists the latest pickups, with buttons to jump straight
 * to booking a pickup or checking rewards.
 */
public class HomePanel extends JPanel implements Refreshable {
    private static final String WELCOME = "Welcome to CWRM";
    private static final String INTRO = "Book a recycling or bulky-waste pickup in under a minute, follow it"
            + " until it is collected, and earn reward points for every kilogram you recycle.";
    private static final String[][] STEPS = {
        {"1   Book a pickup", "Choose your name, address, material and a day. Not sure of the address?"
            + " Use Find address to check it."},
        {"2   A collector picks it up", "On the day, a collector weighs what was collected and marks"
            + " your pickup as completed."},
        {"3   Earn reward points", "Points are added to your account straight away. See and redeem"
            + " them on My rewards."}};
    private static final int STEP_TITLE = 0;
    private static final int STEP_TEXT = 1;
    private static final int STEP_TEXT_LINES = 2;
    private static final String[] COLUMNS = {"ID", "Resident", "Material", "Pickup day", "Status"};

    private final PickupController controller;
    private final StatCard bookedCard = new StatCard("Pickups booked", Theme.INFO);
    private final StatCard waitingCard = new StatCard("Waiting for a collector", Theme.PENDING);
    private final StatCard kgCard = new StatCard("Recycled so far", Theme.PRIMARY);
    private final StatCard pointsCard = new StatCard("Points given to residents", Theme.PRIMARY);
    private final StatCard rateCard = new StatCard("Pickups completed", Theme.INFO);
    private final ReadOnlyTableModel recentRows = new ReadOnlyTableModel(COLUMNS);

    /**
     * Creates the home screen.
     *
     * @param controller   supplies the figures and the latest pickups
     * @param openBooking  opens the booking screen
     * @param openRewards  opens the rewards screen
     */
    public HomePanel(PickupController controller, Runnable openBooking, Runnable openRewards) {
        super(new BorderLayout(Theme.GAP, Theme.GAP));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));
        add(topSection(openBooking, openRewards), BorderLayout.NORTH);
        add(new CardPanel("Recent activity", new JScrollPane(new StyledTable(recentRows))), BorderLayout.CENTER);
        refresh();
    }

    private JPanel topSection(Runnable openBooking, Runnable openRewards) {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(welcome(openBooking, openRewards));
        top.add(row(stepCards()));
        top.add(row(bookedCard, waitingCard, kgCard, pointsCard, rateCard));
        return top;
    }

    private static JPanel welcome(Runnable openBooking, Runnable openRewards) {
        JPanel welcome = new JPanel(new BorderLayout(0, Theme.SMALL_GAP));
        welcome.setOpaque(false);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP, 0));
        welcome.add(Widgets.text(WELCOME, Widgets.HUGE), BorderLayout.NORTH);
        welcome.add(new JLabel(INTRO), BorderLayout.CENTER);
        welcome.add(quickActions(openBooking, openRewards), BorderLayout.SOUTH);
        return welcome;
    }

    private static JPanel quickActions(Runnable openBooking, Runnable openRewards) {
        JButton book = Widgets.primaryButton("Book a pickup");
        book.addActionListener(event -> openBooking.run());
        JButton rewards = new JButton("See my rewards");
        rewards.addActionListener(event -> openRewards.run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(book);
        actions.add(Box.createHorizontalStrut(Theme.GAP));
        actions.add(rewards);
        return actions;
    }

    private static JComponent[] stepCards() {
        JComponent[] cards = new JComponent[STEPS.length];
        for (int step = 0; step < STEPS.length; step++) {
            JLabel title = Widgets.text(STEPS[step][STEP_TITLE], Widgets.SUBTITLE);
            title.setForeground(Theme.PRIMARY_DARK);
            JTextArea text = Widgets.paragraph(STEPS[step][STEP_TEXT]);
            text.setRows(STEP_TEXT_LINES);
            JPanel card = new JPanel(new BorderLayout(0, Theme.SMALL_GAP));
            Widgets.styleAsCard(card);
            card.add(title, BorderLayout.NORTH);
            card.add(text, BorderLayout.CENTER);
            cards[step] = card;
        }
        return cards;
    }

    /** Puts components side by side in equal widths, with a gap below the row. */
    private static JPanel row(JComponent... components) {
        JPanel row = new JPanel(new GridLayout(1, 0, Theme.GAP, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP, 0));
        for (JComponent component : components) {
            row.add(component);
        }
        return row;
    }

    @Override
    public void refresh() {
        StatisticsSummary summary = controller.statistics();
        bookedCard.setValue(String.valueOf(summary.totalPickups()));
        waitingCard.setValue(String.valueOf(summary.count(PickupStatus.PENDING)));
        kgCard.setValue(Formats.kg(summary.totalKg()));
        pointsCard.setValue(Formats.points(summary.pointsAwarded()));
        rateCard.setValue(Formats.percent(summary.completionRatePercent()));
        recentRows.setRows(controller.recentPickups().stream().map(HomePanel::toRow).toList());
    }

    private static Object[] toRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getResident().getName(), request.getMaterial(),
            request.getDate(), request.getStatus()};
    }
}
