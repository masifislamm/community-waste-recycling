package cwrm.view;

import cwrm.api.NominatimAddressLookup.LookupResult;
import cwrm.controller.BookingForm;
import cwrm.controller.Outcome;
import cwrm.controller.PickupController;
import cwrm.model.Material;
import cwrm.model.PickupRequest;
import cwrm.model.Resident;
import cwrm.model.TimeSlot;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Resident screen for booking a pickup. The form is split into three short steps.
 * "Find address" checks the address with the Nominatim service in the background, the
 * points estimate updates as the resident types, and "Book pickup" sends the form to the
 * controller. The searchable table on the right lists every pickup booked so far
 * (the collector screen shows each pickup's address).
 */
public class BookingPanel extends JPanel implements Refreshable {
    private static final String FORM_TITLE = "Book a pickup";
    private static final String TABLE_TITLE = "All pickups (%d)";
    private static final String ANY_TIME = "Any time";
    private static final String SEARCHING = "Searching for the address...";
    private static final String LOOKUP_FAILED = "The address search did not finish. Please try again.";
    private static final String NOT_CHECKED = "Tip: click Find address to check it on OpenStreetMap.";
    private static final String FOUND = "Found on the map at (%.5f, %.5f).";
    private static final String LAST_KNOWN = "Showing the last address found, at (%.5f, %.5f).";
    private static final String FORM_CLEARED = "The form has been cleared.";
    private static final String REQUIRED_NOTE = "Fields marked * are required.";
    private static final int DAYS_TO_OFFER = 14;
    private static final int TOMORROW = 1;
    private static final int FORM_WIDTH = 520;
    private static final int HINT_WIDTH = 250;
    private static final int[] COLUMN_WIDTHS = {60, 120, 90, 90, 170, 110};
    private static final String[] COLUMNS = {"ID", "Resident", "Material", "Pickup day", "Time slot", "Status"};

    private final PickupController controller;
    private final JComboBox<Resident> residentBox = new JComboBox<>();
    private final JTextField addressField = Widgets.field(0, "e.g. Menara Kuala Lumpur");
    private final JButton findButton = new JButton("Find address");
    private final JLabel matchedLabel = Widgets.hint(Widgets.wrapped(NOT_CHECKED, HINT_WIDTH));
    private final JComboBox<Material> materialBox = new JComboBox<>(Material.values());
    private final JLabel materialHint = Widgets.hint("");
    private final JComboBox<LocalDate> dayBox = new JComboBox<>();
    private final JComboBox<Object> timeSlotBox = new JComboBox<>();
    private final JTextField weightField = Widgets.field(0, "e.g. 5");
    private final JLabel estimateLabel = Widgets.hint("");
    private final JTextField notesField = Widgets.field(0, "e.g. Bags are next to the gate");
    private final JTextField searchField = Widgets.searchField("Search by ID, name, material or status");
    private final ReadOnlyTableModel pickupRows = new ReadOnlyTableModel(COLUMNS);
    private final StyledTable pickupTable = new StyledTable(pickupRows);
    private final CardPanel tableCard = new CardPanel(TABLE_TITLE, tableArea());
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the booking screen.
     *
     * @param controller handles every action on this screen
     */
    public BookingPanel(PickupController controller) {
        super(new BorderLayout(Theme.GAP, Theme.GAP));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));
        fillChoices();
        listenForChanges();
        CardPanel formCard = new CardPanel(FORM_TITLE, Widgets.scrollable(buildForm()));
        formCard.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        add(formCard, BorderLayout.WEST);
        pickupTable.setPreferredColumnWidths(COLUMN_WIDTHS);
        add(tableCard, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private void fillChoices() {
        controller.residents().forEach(residentBox::addItem);
        residentBox.setRenderer(new LabelRenderer<>(Resident.class, Resident::getName));
        timeSlotBox.addItem(ANY_TIME);
        for (TimeSlot slot : TimeSlot.values()) {
            timeSlotBox.addItem(slot);
        }
        LocalDate today = LocalDate.now();
        for (int day = 0; day < DAYS_TO_OFFER; day++) {
            dayBox.addItem(today.plusDays(day));
        }
        dayBox.setRenderer(new LabelRenderer<>(LocalDate.class, Formats::day));
        dayBox.setSelectedIndex(TOMORROW);
    }

    private void listenForChanges() {
        findButton.addActionListener(event -> findAddress());
        materialBox.addActionListener(event -> showMaterialHelp());
        Widgets.onTextChange(weightField, this::showEstimate);
        Widgets.onTextChange(searchField, () -> pickupTable.showRowsContaining(searchField.getText()));
    }

    private JPanel buildForm() {
        FormPanel form = new FormPanel();
        form.addSection("1. Who and where");
        form.addRow("Resident *", residentBox);
        form.addRow("Address *", addressRow());
        form.addHint(matchedLabel);
        form.addSection("2. What and when");
        form.addRow("Material *", materialBox);
        form.addHint(materialHint);
        form.addRow("Pickup day *", dayBox);
        form.addRow("Time slot", timeSlotBox);
        form.addSection("3. Extra details (optional)");
        form.addRow("Estimated weight (kg)", weightField);
        form.addHint(estimateLabel);
        form.addRow("Notes for collector", notesField);
        addActions(form);
        return form;
    }

    private void addActions(FormPanel form) {
        JButton bookButton = Widgets.primaryButton("Book pickup");
        bookButton.addActionListener(event -> bookPickup());
        JButton clearButton = new JButton("Clear form");
        clearButton.addActionListener(event -> clearForm());
        form.addButtons(bookButton, clearButton);
        form.addHint(Widgets.hint(REQUIRED_NOTE));
    }

    private JPanel addressRow() {
        JPanel row = new JPanel(new BorderLayout(Theme.SMALL_GAP, 0));
        row.setOpaque(false);
        row.add(addressField, BorderLayout.CENTER);
        row.add(findButton, BorderLayout.EAST);
        return row;
    }

    private JPanel tableArea() {
        JPanel area = new JPanel(new BorderLayout(0, Theme.SMALL_GAP));
        area.setOpaque(false);
        area.add(searchField, BorderLayout.NORTH);
        area.add(new JScrollPane(pickupTable), BorderLayout.CENTER);
        return area;
    }

    /** Runs the address search on a background thread so the window stays responsive. */
    private void findAddress() {
        findButton.setEnabled(false);
        status.showInfo(SEARCHING);
        String typed = addressField.getText();
        new SwingWorker<LookupResult, Void>() {
            @Override
            protected LookupResult doInBackground() {
                return controller.findAddress(typed);
            }

            @Override
            protected void done() {
                findButton.setEnabled(true);
                showLookupResult(this);
            }
        }.execute();
    }

    private void showLookupResult(SwingWorker<LookupResult, Void> search) {
        try {
            LookupResult result = search.get();
            status.showOutcome(new Outcome(result.found(), result.message()));
            if (result.hasAddress()) {
                showMatchedAddress(result);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            status.showProblem(LOOKUP_FAILED);
        } catch (ExecutionException failed) {
            status.showProblem(LOOKUP_FAILED);
        }
    }

    private void showMatchedAddress(LookupResult result) {
        addressField.setText(result.address().displayName());
        addressField.setCaretPosition(0);
        String format = result.found() ? FOUND : LAST_KNOWN;
        matchedLabel.setText(String.format(format, result.address().latitude(), result.address().longitude()));
        matchedLabel.setForeground(result.found() ? Theme.PRIMARY_DARK : Theme.PENDING);
    }

    private void showMaterialHelp() {
        Material material = (Material) materialBox.getSelectedItem();
        materialHint.setText(material == null ? "" : Widgets.wrapped(material.getExamples(), HINT_WIDTH));
        showEstimate();
    }

    /** Asks the controller how many points this booking could earn and shows the answer. */
    private void showEstimate() {
        Outcome estimate = controller.estimatePoints((Material) materialBox.getSelectedItem(), weightField.getText());
        estimateLabel.setText(Widgets.wrapped(estimate.message(), HINT_WIDTH));
        estimateLabel.setForeground(estimate.success() ? Theme.PRIMARY_DARK : Theme.MUTED);
    }

    private void bookPickup() {
        Outcome outcome = controller.bookPickup(readForm());
        status.showOutcome(outcome);
        if (outcome.success()) {
            weightField.setText("");
            notesField.setText("");
            refresh();
        }
    }

    private void clearForm() {
        addressField.setText("");
        matchedLabel.setText(Widgets.wrapped(NOT_CHECKED, HINT_WIDTH));
        matchedLabel.setForeground(Theme.MUTED);
        materialBox.setSelectedIndex(0);
        dayBox.setSelectedIndex(TOMORROW);
        timeSlotBox.setSelectedIndex(0);
        weightField.setText("");
        notesField.setText("");
        status.showInfo(FORM_CLEARED);
    }

    private BookingForm readForm() {
        Object slot = timeSlotBox.getSelectedItem();
        LocalDate day = (LocalDate) dayBox.getSelectedItem();
        return new BookingForm((Resident) residentBox.getSelectedItem(), addressField.getText(),
                (Material) materialBox.getSelectedItem(), day == null ? "" : day.toString(),
                weightField.getText(), notesField.getText(), slot instanceof TimeSlot chosen ? chosen : null);
    }

    @Override
    public void refresh() {
        pickupRows.setRows(controller.allPickups().stream().map(BookingPanel::toRow).toList());
        tableCard.setTitle(String.format(TABLE_TITLE, pickupRows.getRowCount()));
        showMaterialHelp();
    }

    private static Object[] toRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getResident().getName(), request.getMaterial(),
            request.getDate(),
            request.getTimeSlot().map(TimeSlot::getLabel).orElse(ANY_TIME), request.getStatus()};
    }
}
