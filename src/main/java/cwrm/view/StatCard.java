package cwrm.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A small card that shows one figure in large text with a caption under it,
 * for example "12.5 kg" above "Recycled so far".
 */
public class StatCard extends JPanel {
    private static final String NO_VALUE = "-";

    private final JLabel valueLabel = Widgets.text(NO_VALUE, Widgets.LARGE_TITLE);

    /**
     * Creates a figure card.
     *
     * @param caption what the figure means
     * @param colour  the colour of the figure
     */
    public StatCard(String caption, Color colour) {
        super(new BorderLayout());
        Widgets.styleAsCard(this);
        valueLabel.setForeground(colour);
        add(valueLabel, BorderLayout.CENTER);
        add(Widgets.hint(caption), BorderLayout.SOUTH);
    }

    /**
     * Shows a new figure.
     *
     * @param value the figure, already formatted, for example {@code "75.0 %"}
     */
    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
