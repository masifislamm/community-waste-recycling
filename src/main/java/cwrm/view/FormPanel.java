package cwrm.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that lines up labels and input fields in two tidy columns,
 * so every screen has the same clean form layout. Sections and hints break
 * a long form into small, easy steps.
 */
public class FormPanel extends JPanel {
    private static final int GAP = 4;
    private static final int SECTION_GAP = 14;
    private static final Insets CELL_GAP = new Insets(GAP, GAP, GAP, GAP);
    private static final Insets SECTION_INSETS = new Insets(SECTION_GAP, GAP, GAP, GAP);
    private static final Insets HINT_INSETS = new Insets(0, GAP, GAP, GAP);
    private static final int LABEL_COLUMN = 0;
    private static final int FIELD_COLUMN = 1;
    private static final int BOTH_COLUMNS = 2;
    private static final int BUTTON_GAP = 8;

    private int nextRow;

    /** Creates an empty form with a see-through background, so it sits neatly inside a card. */
    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    /**
     * Adds a section heading across both columns, for example "1. Who and where".
     *
     * @param title the heading text
     */
    public void addSection(String title) {
        GridBagConstraints constraints = cell(LABEL_COLUMN, GridBagConstraints.HORIZONTAL, 1);
        constraints.gridwidth = BOTH_COLUMNS;
        constraints.insets = nextRow == 0 ? CELL_GAP : SECTION_INSETS;
        add(Widgets.text(title, Widgets.SUBTITLE), constraints);
        nextRow++;
    }

    /**
     * Adds one row: a label on the left and a field on the right.
     *
     * @param label the label text
     * @param field the input field or value shown next to the label
     */
    public void addRow(String label, JComponent field) {
        add(new JLabel(label), cell(LABEL_COLUMN, GridBagConstraints.NONE, 0));
        add(field, cell(FIELD_COLUMN, GridBagConstraints.HORIZONTAL, 1));
        nextRow++;
    }

    /**
     * Adds a small hint or live message under the field above it.
     *
     * @param hint the hint label
     */
    public void addHint(JComponent hint) {
        GridBagConstraints constraints = cell(FIELD_COLUMN, GridBagConstraints.HORIZONTAL, 1);
        constraints.insets = HINT_INSETS;
        add(hint, constraints);
        nextRow++;
    }

    /**
     * Adds a note across both columns, for example an explanation at the top of a small form.
     *
     * @param note the note label
     */
    public void addNote(JComponent note) {
        GridBagConstraints constraints = cell(LABEL_COLUMN, GridBagConstraints.HORIZONTAL, 1);
        constraints.gridwidth = BOTH_COLUMNS;
        add(note, constraints);
        nextRow++;
    }

    /**
     * Adds a row of buttons under the fields, at their natural size.
     *
     * @param buttons the buttons, left to right
     */
    public void addButtons(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        for (JButton button : buttons) {
            row.add(button);
            row.add(Box.createHorizontalStrut(BUTTON_GAP));
        }
        GridBagConstraints constraints = cell(FIELD_COLUMN, GridBagConstraints.NONE, 1);
        constraints.insets = SECTION_INSETS;
        add(row, constraints);
        nextRow++;
    }

    private GridBagConstraints cell(int column, int fill, double widthShare) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = nextRow;
        constraints.fill = fill;
        constraints.weightx = widthShare;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = CELL_GAP;
        return constraints;
    }
}
