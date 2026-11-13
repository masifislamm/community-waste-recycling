package cwrm.view;

import cwrm.controller.PickupController;
import cwrm.model.PickupRequest;
import cwrm.model.Resident;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Resident screen for tracking pickups and reward points. The resident picks their
 * name to see their points balance and the status of each pickup they booked.
 */
public class RewardsPanel extends JPanel implements Refreshable {
    private static final String TITLE = "My pickups and reward points";
    private static final String BALANCE_FORMAT = "%d points";
    private static final String NO_RESIDENT = "-";
    private static final String[] COLUMNS = {"ID", "Material", "Date", "Status"};

    private final PickupController controller;
    private final JComboBox<Resident> residentBox = new JComboBox<>();
    private final JLabel balanceLabel = new JLabel(NO_RESIDENT);
    private final JLabel ruleLabel = new JLabel(NO_RESIDENT);
    private final ReadOnlyTableModel pickupRows = new ReadOnlyTableModel(COLUMNS);

    /**
     * Creates the rewards screen.
     *
     * @param controller supplies the residents and their pickups
     */
    public RewardsPanel(PickupController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setBorder(BorderFactory.createTitledBorder(TITLE));
        controller.residents().forEach(residentBox::addItem);
        residentBox.setRenderer(new LabelRenderer<>(Resident.class, Resident::getName));
        residentBox.addActionListener(event -> refresh());
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(pickupRows)), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Resident", residentBox);
        form.addRow("Points balance", balanceLabel);
        form.addRow("Current reward rule", ruleLabel);
        return form;
    }

    @Override
    public void refresh() {
        ruleLabel.setText(controller.activeRule().getName());
        Resident resident = (Resident) residentBox.getSelectedItem();
        if (resident == null) {
            return;
        }
        balanceLabel.setText(String.format(BALANCE_FORMAT, resident.getAccount().getBalance()));
        pickupRows.setRows(controller.pickupsFor(resident).stream().map(RewardsPanel::toRow).toList());
    }

    private static Object[] toRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getMaterial(), request.getDate(), request.getStatus()};
    }
}
