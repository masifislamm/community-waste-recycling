package cwrm.view;

import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Table data that users can see and select but not edit.
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
