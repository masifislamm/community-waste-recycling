package cwrm.view;

import cwrm.controller.Outcome;
import cwrm.controller.PickupController;
import cwrm.model.CollectionLog;
import cwrm.model.PickupRequest;
import cwrm.model.Resident;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Resident screen for tracking pickups and reward points. The resident picks their name to see
 * their points balance, how much they have recycled, what each pickup earned, and can redeem points.
 */
public class RewardsPanel extends JPanel implements Refreshable {
    private static final String TABLE_TITLE = "My pickups";
    private static final String REDEEM_TITLE = "Redeem points";
    private static final String REDEEM_INFO = "Exchange your points for rewards at the community recycling"
            + " centre, for example shopping vouchers. Enter how many points to use.";
    private static final String HOW_POINTS_WORK = "How points work: when a collector completes your pickup,"
            + " you earn points for the weight collected using the current reward rule: %s.";
    private static final String NOT_YET = "-";
    private static final String EARNED = "+%d";
    private static final String KG_VALUE = "%.1f";
    private static final int SIDE_WIDTH = 400;
    private static final int TEXT_WIDTH = 250;
    private static final String[] COLUMNS = {"ID", "Material", "Pickup day", "Status", "Kg collected",
        "Points earned"};

    private final PickupController controller;
    private final JComboBox<Resident> residentBox = new JComboBox<>();
    private final StatCard balanceCard = new StatCard("Points balance", Theme.PRIMARY);
    private final StatCard kgCard = new StatCard("Recycled so far", Theme.PRIMARY);
    private final StatCard bookedCard = new StatCard("Pickups booked", Theme.INFO);
    private final JTextField pointsField = Widgets.field(0, "e.g. 20");
    private final JLabel ruleLabel = Widgets.hint("");
    private final ReadOnlyTableModel pickupRows = new ReadOnlyTableModel(COLUMNS);
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the rewards screen.
     *
     * @param controller supplies the residents, their pickups and redeems points
     */
    public RewardsPanel(PickupController controller) {
        super(new BorderLayout(Theme.GAP, Theme.GAP));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));
        controller.residents().forEach(residentBox::addItem);
        residentBox.setRenderer(new LabelRenderer<>(Resident.class, Resident::getName));
        residentBox.addActionListener(event -> refresh());
        add(topSection(), BorderLayout.NORTH);
        add(new CardPanel(TABLE_TITLE, new JScrollPane(new StyledTable(pickupRows))), BorderLayout.CENTER);
        add(redeemCard(), BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel topSection() {
        JPanel chooser = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SMALL_GAP, 0));
        chooser.setOpaque(false);
        chooser.add(Widgets.text("Showing rewards for", Widgets.SUBTITLE));
        chooser.add(residentBox);
        JPanel cards = new JPanel(new GridLayout(1, 0, Theme.GAP, 0));
        cards.setOpaque(false);
        cards.add(balanceCard);
        cards.add(kgCard);
        cards.add(bookedCard);
        JPanel top = new JPanel(new BorderLayout(0, Theme.GAP));
        top.setOpaque(false);
        top.add(chooser, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        return top;
    }

    private CardPanel redeemCard() {
        JButton redeemButton = Widgets.primaryButton("Redeem points");
        redeemButton.addActionListener(event -> redeemPoints());
        FormPanel form = new FormPanel();
        form.addNote(new JLabel(Widgets.wrapped(REDEEM_INFO, TEXT_WIDTH)));
        form.addRow("Points *", pointsField);
        form.addButtons(redeemButton);
        form.addSection("Good to know");
        form.addNote(ruleLabel);
        CardPanel card = new CardPanel(REDEEM_TITLE, Widgets.scrollable(form));
        card.setPreferredSize(new Dimension(SIDE_WIDTH, 0));
        return card;
    }

    private void redeemPoints() {
        Outcome outcome = controller.redeemPoints(selectedResident(), pointsField.getText());
        status.showOutcome(outcome);
        if (outcome.success()) {
            pointsField.setText("");
            refresh();
        }
    }

    private Resident selectedResident() {
        return (Resident) residentBox.getSelectedItem();
    }

    @Override
    public void refresh() {
        ruleLabel.setText(Widgets.wrapped(String.format(HOW_POINTS_WORK, controller.activeRule().getName()),
                TEXT_WIDTH));
        Resident resident = selectedResident();
        if (resident == null) {
            return;
        }
        List<PickupRequest> pickups = controller.pickupsFor(resident);
        balanceCard.setValue(Formats.points(resident.getAccount().getBalance()));
        kgCard.setValue(Formats.kg(controller.kgRecycledBy(resident)));
        bookedCard.setValue(String.valueOf(pickups.size()));
        pickupRows.setRows(pickups.stream().map(this::toRow).toList());
    }

    private Object[] toRow(PickupRequest request) {
        Optional<CollectionLog> log = controller.logFor(request.getId());
        return new Object[] {request.getId(), request.getMaterial(), request.getDate(), request.getStatus(),
            log.map(entry -> String.format(KG_VALUE, entry.actualWeightKg())).orElse(NOT_YET),
            log.map(entry -> String.format(EARNED, entry.pointsAwarded())).orElse(NOT_YET)};
    }
}
