package cwrm.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that lines up labels and input fields in two tidy columns,
 * so every screen has the same clean form layout.
 */
public class FormPanel extends JPanel {
    private static final int GAP = 5;
    private static final Insets CELL_GAP = new Insets(GAP, GAP, GAP, GAP);
    private static final int LABEL_COLUMN = 0;
    private static final int FIELD_COLUMN = 1;

    private int nextRow;

    /** Creates an empty form. */
    public FormPanel() {
        super(new GridBagLayout());
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
     * Adds a row of buttons under the fields, at their natural size.
     *
     * @param buttons the buttons, left to right
     */
    public void addButtons(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (JButton button : buttons) {
            row.add(button);
        }
        add(row, cell(FIELD_COLUMN, GridBagConstraints.NONE, 1));
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
