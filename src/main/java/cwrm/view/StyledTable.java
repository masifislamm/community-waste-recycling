package cwrm.view;

import cwrm.model.PickupStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

/**
 * The table used on every screen. Users can sort by clicking a column heading, filter rows
 * with a search box, see each pickup's status as a coloured badge and read dates in a friendly format.
 */
public class StyledTable extends JTable {
    private static final String IGNORE_CASE = "(?i)";
    private static final int NO_SELECTION = -1;

    private final ReadOnlyTableModel rows;
    private final TableRowSorter<ReadOnlyTableModel> sorter;

    /**
     * Creates a table that shows the given rows.
     *
     * @param rows the table data
     */
    public StyledTable(ReadOnlyTableModel rows) {
        super(rows);
        this.rows = rows;
        this.sorter = new TableRowSorter<>(rows);
        setRowSorter(sorter);
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        setDefaultRenderer(PickupStatus.class, new StatusBadgeRenderer());
        setDefaultRenderer(LocalDate.class, new DateRenderer());
        setDefaultRenderer(LocalDateTime.class, new DateRenderer());
    }

    /**
     * Shows only the rows that contain the text in any column, ignoring upper and lower case.
     *
     * @param text the text to look for; blank shows every row
     */
    public void showRowsContaining(String text) {
        if (text == null || text.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter(IGNORE_CASE + Pattern.quote(text.trim())));
    }

    /**
     * Suggests how wide each column should be; the table shares any extra space in proportion.
     *
     * @param widths a width in pixels for each column, left to right
     */
    public void setPreferredColumnWidths(int... widths) {
        for (int column = 0; column < widths.length; column++) {
            getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }
    }

    /**
     * Finds the selected row in the original data, even when the table is sorted or filtered.
     *
     * @return the row's position in the data, or -1 if no row is selected
     */
    public int selectedDataRow() {
        int viewRow = getSelectedRow();
        return viewRow == NO_SELECTION ? NO_SELECTION : convertRowIndexToModel(viewRow);
    }

    /** @return the data behind this table */
    public ReadOnlyTableModel rows() {
        return rows;
    }
}
