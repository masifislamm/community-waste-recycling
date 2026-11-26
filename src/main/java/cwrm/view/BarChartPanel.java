package cwrm.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 * A simple horizontal bar chart drawn with Java 2D (no chart library).
 * Each row shows a label, a bar as long as its value compared with the largest value,
 * and the value as text.
 */
public class BarChartPanel extends JComponent {
    private static final int ROW_HEIGHT = 36;
    private static final int BAR_HEIGHT = 16;
    private static final int LABEL_WIDTH = 110;
    private static final int VALUE_WIDTH = 80;
    private static final int MIN_BAR_AREA = 160;
    private static final int GAP = 8;
    private static final int SIDES = 2;
    private static final Color TRACK_COLOUR = new Color(0xEEF2EF);

    private final String valueFormat;
    private final Color barColour;
    private Map<String, Double> bars = new LinkedHashMap<>();

    /**
     * Creates an empty chart.
     *
     * @param valueFormat how each value is written, for example {@code "%.1f kg"}
     * @param barColour   the colour of the bars
     */
    public BarChartPanel(String valueFormat, Color barColour) {
        this.valueFormat = valueFormat;
        this.barColour = barColour;
    }

    /**
     * Replaces the bars shown.
     *
     * @param newBars a label and a value (0 or more) for each bar, in the order to show them
     */
    public void setBars(Map<String, Double> newBars) {
        bars = new LinkedHashMap<>(newBars);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(LABEL_WIDTH + MIN_BAR_AREA + VALUE_WIDTH, bars.size() * ROW_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(getFont());
        double largest = bars.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        int rowHeight = bars.isEmpty() ? ROW_HEIGHT : Math.min(ROW_HEIGHT, getHeight() / bars.size());
        int row = 0;
        for (Map.Entry<String, Double> bar : bars.entrySet()) {
            paintRow(g, row * rowHeight, rowHeight, bar, largest);
            row++;
        }
        g.dispose();
    }

    private void paintRow(Graphics2D g, int top, int rowHeight, Map.Entry<String, Double> bar, double largest) {
        int barArea = Math.max(0, getWidth() - LABEL_WIDTH - VALUE_WIDTH);
        int barHeight = Math.min(BAR_HEIGHT, rowHeight / SIDES);
        int barTop = top + (rowHeight - barHeight) / SIDES;
        g.setColor(TRACK_COLOUR);
        g.fillRoundRect(LABEL_WIDTH, barTop, barArea, barHeight, barHeight, barHeight);
        int barWidth = largest > 0 ? (int) Math.round(barArea * bar.getValue() / largest) : 0;
        g.setColor(barColour);
        g.fillRoundRect(LABEL_WIDTH, barTop, barWidth, barHeight, barHeight, barHeight);
        int baseline = textBaseline(g.getFontMetrics(), top, rowHeight);
        g.setColor(getForeground());
        g.drawString(bar.getKey(), 0, baseline);
        g.drawString(String.format(valueFormat, bar.getValue()), LABEL_WIDTH + barArea + GAP, baseline);
    }

    private static int textBaseline(FontMetrics metrics, int rowTop, int rowHeight) {
        return rowTop + (rowHeight + metrics.getAscent() - metrics.getDescent()) / SIDES;
    }
}
