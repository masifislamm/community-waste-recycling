package cwrm.view;

import cwrm.controller.Outcome;
import cwrm.controller.PickupController;
import cwrm.model.CollectionLog;
import cwrm.model.Collector;
import cwrm.model.PickupRequest;
import cwrm.model.TimeSlot;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Collector screen. The collector selects a waiting pickup, checks its details on the right,
 * enters the weight actually collected and completes it (or cancels it after confirming).
 * The lower table is the collection log.
 */
public class CollectorPanel extends JPanel implements Refreshable {
    private static final String PENDING_TITLE = "Waiting for collection (%d)";
    private static final String LOG_TITLE = "Collection log (%d)";
    private static final String DETAILS_TITLE = "Selected pickup";
    private static final String NOT_GIVEN = "-";
    private static final String ANY_TIME = "Any time";
    private static final String SELECT_HINT = "Select a pickup in the list to see its details here.";
    private static final String CONFIRM_CANCEL = "Cancel pickup %s for %s?%nThe resident will not earn points for it.";
    private static final String CONFIRM_TITLE = "Cancel pickup";
    private static final int DETAILS_WIDTH = 440;
    private static final int TEXT_WIDTH = 220;
    private static final String[] PENDING_COLUMNS = {"ID", "Resident", "Material", "Pickup day", "Time slot",
        "Est. kg"};
    private static final String[] LOG_COLUMNS = {"Pickup", "Collector", "Kg collected", "Points", "Completed at"};

    private final PickupController controller;
    private final JComboBox<Collector> collectorBox = new JComboBox<>();
    private final JTextField weightField = Widgets.field(0, "e.g. 5.5");
    private final ReadOnlyTableModel pendingRows = new ReadOnlyTableModel(PENDING_COLUMNS);
    private final StyledTable pendingTable = new StyledTable(pendingRows);
    private final ReadOnlyTableModel logRows = new ReadOnlyTableModel(LOG_COLUMNS);
    private final CardPanel pendingCard = new CardPanel(PENDING_TITLE, new JScrollPane(pendingTable));
    private final CardPanel logCard = new CardPanel(LOG_TITLE, new JScrollPane(new StyledTable(logRows)));
    private final JLabel residentValue = new JLabel(NOT_GIVEN);
    private final JLabel addressValue = new JLabel(NOT_GIVEN);
    private final JLabel materialValue = new JLabel(NOT_GIVEN);
    private final JLabel dayValue = new JLabel(NOT_GIVEN);
    private final JLabel slotValue = new JLabel(NOT_GIVEN);
    private final JLabel notesValue = new JLabel(NOT_GIVEN);
    private final JLabel selectHint = Widgets.hint(SELECT_HINT);
    private final CardPanel detailsCard = new CardPanel(DETAILS_TITLE, Widgets.scrollable(buildDetails()));
    private final StatusLabel status = new StatusLabel();
    private List<PickupRequest> pending = List.of();

    /**
     * Creates the collector screen.
     *
     * @param controller handles every action on this screen
     */
    public CollectorPanel(PickupController controller) {
        super(new BorderLayout(Theme.GAP, Theme.GAP));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));
        controller.collectors().forEach(collectorBox::addItem);
        collectorBox.setRenderer(new LabelRenderer<>(Collector.class, Collector::getName));
        pendingTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showDetails(selectedPickup());
            }
        });
        detailsCard.setPreferredSize(new Dimension(DETAILS_WIDTH, 0));
        add(tables(), BorderLayout.CENTER);
        add(detailsCard, BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel tables() {
        JPanel tables = new JPanel(new GridLayout(0, 1, 0, Theme.GAP));
        tables.setOpaque(false);
        tables.add(pendingCard);
        tables.add(logCard);
        return tables;
    }

    private JPanel buildDetails() {
        FormPanel form = new FormPanel();
        form.addNote(selectHint);
        form.addRow("Resident", residentValue);
        form.addRow("Address", addressValue);
        form.addRow("Material", materialValue);
        form.addRow("Pickup day", dayValue);
        form.addRow("Time slot", slotValue);
        form.addRow("Notes", notesValue);
        form.addSection("Record the collection");
        form.addRow("Collector *", collectorBox);
        form.addRow("Weight (kg) *", weightField);
        addActions(form);
        return form;
    }

    private void addActions(FormPanel form) {
        JButton completeButton = Widgets.primaryButton("Complete pickup");
        completeButton.addActionListener(event -> completePickup());
        JButton cancelButton = new JButton("Cancel pickup");
        cancelButton.addActionListener(event -> cancelPickup());
        form.addButtons(completeButton, cancelButton);
    }

    /** Fills the details card for the selected pickup and suggests its estimated weight. */
    private void showDetails(PickupRequest request) {
        if (request == null) {
            clearDetails();
            return;
        }
        selectHint.setVisible(false);
        detailsCard.setTitle(DETAILS_TITLE + ": " + request.getId());
        residentValue.setText(request.getResident().getName());
        addressValue.setText(Widgets.wrapped(request.getAddress().displayName(), TEXT_WIDTH));
        materialValue.setText(request.getMaterial().getLabel());
        dayValue.setText(Formats.day(request.getDate()));
        slotValue.setText(request.getTimeSlot().map(TimeSlot::getLabel).orElse(ANY_TIME));
        notesValue.setText(request.getNotes().isBlank() ? NOT_GIVEN : Widgets.wrapped(request.getNotes(), TEXT_WIDTH));
        weightField.setText(request.getEstimatedWeightKg().map(String::valueOf).orElse(""));
    }

    private void clearDetails() {
        selectHint.setVisible(true);
        detailsCard.setTitle(DETAILS_TITLE);
        for (JLabel value : List.of(residentValue, addressValue, materialValue, dayValue, slotValue, notesValue)) {
            value.setText(NOT_GIVEN);
        }
    }

    private void completePickup() {
        showAndRefresh(controller.completePickup(idOf(selectedPickup()), selectedCollector(), weightField.getText()));
    }

    /** Asks the collector to confirm before cancelling, because a cancelled pickup cannot be undone. */
    private void cancelPickup() {
        PickupRequest selected = selectedPickup();
        if (selected != null && !confirmCancel(selected)) {
            return;
        }
        showAndRefresh(controller.cancelPickup(idOf(selected), selectedCollector()));
    }

    private boolean confirmCancel(PickupRequest request) {
        String question = String.format(CONFIRM_CANCEL, request.getId(), request.getResident().getName());
        int answer = JOptionPane.showConfirmDialog(this, question, CONFIRM_TITLE, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    private void showAndRefresh(Outcome outcome) {
        status.showOutcome(outcome);
        if (outcome.success()) {
            weightField.setText("");
            refresh();
        }
    }

    /** @return the selected pickup, or {@code null} if no row is selected */
    private PickupRequest selectedPickup() {
        int row = pendingTable.selectedDataRow();
        return row < 0 ? null : pending.get(row);
    }

    private static String idOf(PickupRequest request) {
        return request == null ? null : request.getId();
    }

    private Collector selectedCollector() {
        return (Collector) collectorBox.getSelectedItem();
    }

    @Override
    public void refresh() {
        pending = controller.pendingPickups();
        pendingRows.setRows(pending.stream().map(CollectorPanel::pendingRow).toList());
        logRows.setRows(controller.logs().stream().map(CollectorPanel::logRow).toList());
        pendingCard.setTitle(String.format(PENDING_TITLE, pendingRows.getRowCount()));
        logCard.setTitle(String.format(LOG_TITLE, logRows.getRowCount()));
        showDetails(selectedPickup());
    }

    private static Object[] pendingRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getResident().getName(), request.getMaterial(),
            request.getDate(), request.getTimeSlot().map(TimeSlot::getLabel).orElse(ANY_TIME),
            request.getEstimatedWeightKg().map(String::valueOf).orElse(NOT_GIVEN)};
    }

    private static Object[] logRow(CollectionLog log) {
        return new Object[] {log.requestId(), log.collector().getName(), log.actualWeightKg(),
            log.pointsAwarded(), log.completedAt()};
    }
}
