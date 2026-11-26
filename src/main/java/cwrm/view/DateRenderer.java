package cwrm.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Shows dates in tables as short, friendly text (for example "Fri 27 Nov") while the table
 * keeps the real date, so sorting by a date column is still in date order.
 */
public class DateRenderer extends DefaultTableCellRenderer {

    @Override
    protected void setValue(Object value) {
        if (value instanceof LocalDate date) {
            super.setValue(Formats.shortDay(date));
        } else if (value instanceof LocalDateTime time) {
            super.setValue(Formats.time(time));
        } else {
            super.setValue(value);
        }
    }
}
