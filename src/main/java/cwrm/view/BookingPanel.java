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
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Resident screen for booking a pickup. "Find address" checks the address with the
 * Nominatim service in the background; "Book pickup" sends the form to the controller.
 * The table below lists every pickup booked so far.
 */
public class BookingPanel extends JPanel implements Refreshable {
    private static final String TITLE = "Book a recycling or bulky-waste pickup";
    private static final String ANY_TIME = "Any time";
    private static final String SEARCHING = "Searching for the address...";
    private static final String LOOKUP_FAILED = "The address search did not finish. Please try again.";
    private static final String NOT_CHECKED = "Not checked yet";
    private static final String COORDINATES = "%s  (%.5f, %.5f)";
    private static final int TEXT_COLUMNS = 30;
    private static final String[] COLUMNS = {"ID", "Resident", "Address", "Material", "Date", "Time slot", "Status"};

    private final PickupController controller;
    private final JComboBox<Resident> residentBox = new JComboBox<>();
    private final JTextField addressField = new JTextField(TEXT_COLUMNS);
    private final JButton findButton = new JButton("Find address");
    private final JLabel matchedLabel = new JLabel(NOT_CHECKED);
    private final JComboBox<Material> materialBox = new JComboBox<>(Material.values());
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(1).toString());
    private final JTextField weightField = new JTextField();
    private final JComboBox<Object> timeSlotBox = new JComboBox<>();
    private final JTextField notesField = new JTextField();
    private final ReadOnlyTableModel pickupRows = new ReadOnlyTableModel(COLUMNS);
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the booking screen.
     *
     * @param controller handles every action on this screen
     */
    public BookingPanel(PickupController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setBorder(BorderFactory.createTitledBorder(TITLE));
        fillChoices();
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(pickupRows)), BorderLayout.CENTER);
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
    }

    private JPanel buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Resident", residentBox);
        form.addRow("Address", addressRow());
        form.addRow("Matched address", matchedLabel);
        form.addRow("Material", materialBox);
        form.addRow("Pickup date (yyyy-MM-dd)", dateField);
        form.addRow("Estimated weight in kg (optional)", weightField);
        form.addRow("Time slot (optional)", timeSlotBox);
        form.addRow("Notes for collector (optional)", notesField);
        JButton bookButton = new JButton("Book pickup");
        bookButton.addActionListener(event -> bookPickup());
        form.addButtons(bookButton);
        return form;
    }

    private JPanel addressRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.add(addressField, BorderLayout.CENTER);
        row.add(findButton, BorderLayout.EAST);
        findButton.addActionListener(event -> findAddress());
        return row;
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
        String name = result.address().displayName();
        addressField.setText(name);
        matchedLabel.setText(String.format(COORDINATES, name, result.address().latitude(),
                result.address().longitude()));
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

    private BookingForm readForm() {
        Object slot = timeSlotBox.getSelectedItem();
        return new BookingForm((Resident) residentBox.getSelectedItem(), addressField.getText(),
                (Material) materialBox.getSelectedItem(), dateField.getText(), weightField.getText(),
                notesField.getText(), slot instanceof TimeSlot chosen ? chosen : null);
    }

    @Override
    public void refresh() {
        pickupRows.setRows(controller.allPickups().stream().map(BookingPanel::toRow).toList());
    }

    private static Object[] toRow(PickupRequest request) {
        return new Object[] {request.getId(), request.getResident().getName(),
            request.getAddress().displayName(), request.getMaterial(), request.getDate(),
            request.getTimeSlot().map(Object::toString).orElse(ANY_TIME), request.getStatus()};
    }
}
