package cwrm.view;

import cwrm.controller.Outcome;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * The message bar at the bottom of each screen. It shows the result of the last action:
 * green for success, red for a problem and grey for information, with a coloured stripe
 * on the left so the message stands out.
 */
public class StatusLabel extends JLabel {
    private static final int STRIPE_WIDTH = 4;
    private static final int PADDING = 8;
    private static final String EMPTY = " ";

    /** Creates an empty message bar. */
    public StatusLabel() {
        super(EMPTY);
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING + STRIPE_WIDTH, PADDING, PADDING));
    }

    /**
     * Shows the outcome of an action.
     *
     * @param outcome the outcome returned by the controller
     */
    public void showOutcome(Outcome outcome) {
        if (outcome.success()) {
            showText(outcome.message(), Theme.PRIMARY_DARK, Theme.PRIMARY_LIGHT);
        } else {
            showProblem(outcome.message());
        }
    }

    /**
     * Shows a neutral message, for example while waiting for the address service.
     *
     * @param message the message
     */
    public void showInfo(String message) {
        showText(message, Theme.MUTED, Theme.MUTED_LIGHT);
    }

    /**
     * Shows a problem message in red.
     *
     * @param message the message
     */
    public void showProblem(String message) {
        showText(message, Theme.PROBLEM, Theme.PROBLEM_LIGHT);
    }

    private void showText(String message, Color text, Color background) {
        setOpaque(true);
        setForeground(text);
        setBackground(background);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, STRIPE_WIDTH, 0, 0, text),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        setText(message);
    }
}
