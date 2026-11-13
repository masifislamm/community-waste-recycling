package cwrm.view;

import cwrm.controller.Outcome;
import cwrm.controller.PickupController;
import cwrm.model.CollectionLog;
import cwrm.model.Collector;
import cwrm.model.PickupRequest;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Collector screen. Shows the pickups waiting to be collected; the collector selects one,
 * enters the weight actually collected and completes or cancels it.
 * The lower table shows the collection log.
 */
public class CollectorPanel extends JPanel implements Refreshable {
    private static final String TITLE = "Collect pending pickups";
    private static final String NOT_GIVEN = "-";
    private static final int ID_COLUMN = 0;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] PENDING_COLUMNS =
        {"ID", "Resident", "Address", "Material", "Date", "Time slot", "Est. kg", "Notes"};
    private static final String[] LOG_COLUMNS = {"Pickup", "Collector", "Actual kg", "Points", "Completed at"};

    private final PickupController controller;
    private final JComboBox<Collector> collectorBox = new JComboBox<>();
    private final JTextField weightField = new JTextField();
    private final ReadOnlyTableModel pendingRows = new ReadOnlyTableModel(PENDING_COLUMNS);
    private final JTable pendingTable = new JTable(pendingRows);
    private final ReadOnlyTableModel logRows = new ReadOnlyTableModel(LOG_COLUMNS);
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the collector screen.
     *
     * @param controller handles every action on this screen
     */
    public CollectorPanel(PickupController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setBorder(BorderFactory.createTitledBorder(TITLE));
        controller.collectors().forEach(collectorBox::addItem);
        collectorBox.setRenderer(new LabelRenderer<>(Collector.class, Collector::getName));
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(buildForm(), BorderLayout.NORTH);
        add(buildTables(), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Collector", collectorBox);
        form.addRow("Actual weight collected (kg)", weightField);
        JButton completeButton = new JButton("Complete selected pickup");
        completeButton.addActionListener(event -> completePickup());
        JButton cancelButton = new JButton("Cancel selected pickup");
        cancelButton.addActionListener(event -> cancelPickup());
        form.addButtons(completeButton, cancelButton);
        return form;
    }

    private JPanel buildTables() {
        JPanel tables = new JPanel(new GridLayout(0, 1));
        tables.add(titled("Pending pickups (select one)", new JScrollPane(pendingTable)));
        tables.add(titled("Collection log", new JScrollPane(new JTable(logRows))));
        return tables;
    }

    private static JPanel titled(String title, JScrollPane content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void completePickup() {
        showAndRefresh(controller.completePickup(selectedPickupId(), selectedCollector(), weightField.getText()));
    }

    private void cancelPickup() {
        showAndRefresh(controller.cancelPickup(selectedPickupId(), selectedCollector()));
    }

    private void showAndRefresh(Outcome outcome) {
        status.showOutcome(outcome);
        if (outcome.success()) {
            weightField.setText("");
            refresh();
        }
    }

    /** @return the ID in the selected row, or {@code null} if no row is selected */
    private String selectedPickupId() {
        int row = pendingTable.getSelectedRow();
        return row < 0 ? null : (String) pendingRows.getValueAt(row, ID_COLUMN);
    }

    private Collector selectedCollector() {
        return (Collector) collectorBox.getSelectedItem();
    }

    @Override
    public void refresh() {
        pendingRows.setRows(controller.pendingPickups().stream().map(CollectorPanel::pendingRow).toList());
        logRows.setRows(controller.logs().stream().map(CollectorPanel::logRow).toList());
    }

    private static Object[] pendingRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getResident().getName(),
            request.getAddress().displayName(), request.getMaterial(), request.getDate(),
            request.getTimeSlot().map(Object::toString).orElse(NOT_GIVEN),
            request.getEstimatedWeightKg().map(Object::toString).orElse(NOT_GIVEN), request.getNotes()};
    }

    private static Object[] logRow(CollectionLog log) {
        return new Object[] {log.requestId(), log.collector().getName(), log.actualWeightKg(),
            log.pointsAwarded(), log.completedAt().format(TIME_FORMAT)};
    }
}
