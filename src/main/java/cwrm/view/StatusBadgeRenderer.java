package cwrm.view;

import cwrm.model.PickupStatus;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Draws a pickup's status in a table as a coloured badge: amber for pending,
 * green for completed and grey for cancelled, so the status can be seen at a glance.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {
    private static final int SIDES = 2;
    private static final int TEXT_PADDING = 10;
    private static final int VERTICAL_MARGIN = 5;
    private static final Map<PickupStatus, Color> TEXT_COLOURS = new EnumMap<>(Map.of(
            PickupStatus.PENDING, Theme.PENDING,
            PickupStatus.COMPLETED, Theme.PRIMARY_DARK,
            PickupStatus.CANCELLED, Theme.MUTED));
    private static final Map<PickupStatus, Color> BADGE_COLOURS = new EnumMap<>(Map.of(
            PickupStatus.PENDING, Theme.PENDING_LIGHT,
            PickupStatus.COMPLETED, Theme.PRIMARY_LIGHT,
            PickupStatus.CANCELLED, Theme.MUTED_LIGHT));

    private Color badgeColour;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                   boolean focused, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        setHorizontalAlignment(CENTER);
        setFont(getFont().deriveFont(Font.BOLD));
        badgeColour = value instanceof PickupStatus status ? BADGE_COLOURS.get(status) : null;
        if (value instanceof PickupStatus status) {
            setForeground(TEXT_COLOURS.get(status));
        }
        return this;
    }

    /** Paints the row background, then the rounded badge, then the status text on top. */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (badgeColour != null) {
            paintBadge(g);
        }
        g.dispose();
        ui.paint(graphics, this);
    }

    private void paintBadge(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int textWidth = g.getFontMetrics(getFont()).stringWidth(getText());
        int badgeWidth = Math.min(getWidth(), textWidth + SIDES * TEXT_PADDING);
        int badgeHeight = getHeight() - SIDES * VERTICAL_MARGIN;
        g.setColor(badgeColour);
        g.fillRoundRect((getWidth() - badgeWidth) / SIDES, VERTICAL_MARGIN, badgeWidth, badgeHeight,
                badgeHeight, badgeHeight);
    }
}
