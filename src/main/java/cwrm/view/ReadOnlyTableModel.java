package cwrm.view;

import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Table data that users can see, sort and select but not edit.
 *
 * <p>Every row must use the same type in a column (for example always a {@code Double} for
 * weights), because the column's type decides how it is sorted and drawn.
 */
public class ReadOnlyTableModel extends DefaultTableModel {

    /**
     * Creates an empty table.
     *
     * @param columnNames the column headings
     */
    public ReadOnlyTableModel(String... columnNames) {
        super(columnNames, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /** Reports the type of the values in a column, so numbers sort as numbers and statuses show as badges. */
    @Override
    public Class<?> getColumnClass(int column) {
        if (getRowCount() == 0 || getValueAt(0, column) == null) {
            return Object.class;
        }
        return getValueAt(0, column).getClass();
    }

    /**
     * Replaces every row in the table.
     *
     * @param rows the new rows, one array of cell values per row
     */
    public void setRows(List<Object[]> rows) {
        setRowCount(0);
        for (Object[] row : rows) {
            addRow(row);
        }
    }
}
