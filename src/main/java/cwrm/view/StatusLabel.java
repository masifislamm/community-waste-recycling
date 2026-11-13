package cwrm.view;

import cwrm.controller.Outcome;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * The message line at the bottom of each screen. It shows the result of the last
 * action: green for success, red for a problem, grey for information.
 */
public class StatusLabel extends JLabel {
    private static final Color SUCCESS_COLOUR = new Color(0x1B5E20);
    private static final Color PROBLEM_COLOUR = new Color(0xB71C1C);
    private static final Color INFO_COLOUR = Color.DARK_GRAY;
    private static final int PADDING = 6;
    private static final String EMPTY = " ";

    /** Creates an empty status line. */
    public StatusLabel() {
        super(EMPTY);
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
    }

    /**
     * Shows the outcome of an action.
     *
     * @param outcome the outcome returned by the controller
     */
    public void showOutcome(Outcome outcome) {
        showText(outcome.message(), outcome.success() ? SUCCESS_COLOUR : PROBLEM_COLOUR);
    }

    /**
     * Shows a neutral message, for example while waiting for the address service.
     *
     * @param message the message
     */
    public void showInfo(String message) {
        showText(message, INFO_COLOUR);
    }

    /**
     * Shows a problem message in red.
     *
     * @param message the message
     */
    public void showProblem(String message) {
        showText(message, PROBLEM_COLOUR);
    }

    private void showText(String message, Color colour) {
        setForeground(colour);
        setText(message);
    }
}
