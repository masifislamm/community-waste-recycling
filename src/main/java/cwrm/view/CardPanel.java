package cwrm.view;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A white box with rounded corners, a title at the top and content below.
 * Every screen is built from these cards so related things are grouped together.
 */
public class CardPanel extends JPanel {
    private final JLabel titleLabel;

    /**
     * Creates a card.
     *
     * @param title   the heading shown at the top of the card
     * @param content what the card contains
     */
    public CardPanel(String title, JComponent content) {
        super(new BorderLayout(0, Theme.GAP));
        Widgets.styleAsCard(this);
        titleLabel = Widgets.text(title, Widgets.SUBTITLE);
        add(titleLabel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    /**
     * Changes the heading, for example to show how many rows a table has.
     *
     * @param title the new heading
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
