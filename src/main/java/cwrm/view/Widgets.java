package cwrm.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Small helpers that create the styled labels, buttons and text fields used on every screen,
 * so all screens look the same and the panel classes stay short.
 */
final class Widgets {
    /** FlatLaf text styles, from largest to smallest. */
    static final String HUGE = "h0";
    static final String LARGE_TITLE = "h1";
    static final String TITLE = "h2";
    static final String SUBTITLE = "h3";
    static final String SMALL = "small";

    private static final String PRIMARY_STYLE =
            "background: #2E7D32; foreground: #FFFFFF; hoverBackground: #276C2B; pressedBackground: #1B5E20;"
            + " font: bold; borderWidth: 0; focusWidth: 0";
    private static final String CARD_STYLE = "arc: 16; background: #FFFFFF";
    private static final int CARD_ARC = 16;
    private static final int SCROLL_STEP = 16;
    private static final String WRAPPED_HTML = "<html><div style='width: %dpx'>%s</div></html>";
    private static final float BORDER_WIDTH = 1f;
    private static final Insets CARD_PADDING = new Insets(Theme.GAP, Theme.GAP + Theme.GAP / 2,
            Theme.GAP, Theme.GAP + Theme.GAP / 2);

    private Widgets() {
        // Static helpers only; no instances.
    }

    /** Creates a label in one of the FlatLaf text styles, for example {@link #TITLE}. */
    static JLabel text(String text, String style) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, style);
        return label;
    }

    /** Creates a small grey label for hints under a field. */
    static JLabel hint(String text) {
        JLabel label = text(text, SMALL);
        label.setForeground(Theme.MUTED);
        return label;
    }

    /** Creates a block of text that wraps onto several lines to fit its space. */
    static JTextArea paragraph(String text) {
        JTextArea area = new JTextArea(text);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setBorder(null);
        area.setForeground(Theme.MUTED);
        return area;
    }

    /** Creates the green button used for the main action on a screen. */
    static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE, PRIMARY_STYLE);
        return button;
    }

    /** Creates a text field that shows grey example text while it is empty. */
    static JTextField field(int columns, String placeholder) {
        JTextField field = new JTextField(columns);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return field;
    }

    /** Creates a search box with a magnifier icon and a button that clears it. */
    static JTextField searchField(String placeholder) {
        JTextField field = field(0, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        return field;
    }

    /** Gives a component a white background with rounded corners and a thin border. */
    static void styleAsCard(JComponent component) {
        component.putClientProperty(FlatClientProperties.STYLE, CARD_STYLE);
        component.setBorder(new FlatLineBorder(CARD_PADDING, Theme.CARD_BORDER, BORDER_WIDTH, CARD_ARC));
    }

    /**
     * Turns plain text into label text that wraps onto several lines instead of being cut off.
     *
     * @param text     the text to show
     * @param maxWidth the widest the text may be, in Swing HTML units (each about 4/3 of a screen pixel)
     * @return label text that wraps at that width
     */
    static String wrapped(String text, int maxWidth) {
        String safeText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return String.format(WRAPPED_HTML, maxWidth, safeText);
    }

    /** Keeps a component at the top of its space instead of stretching it to fill the height. */
    static JPanel topAligned(JComponent component) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(component, BorderLayout.NORTH);
        return holder;
    }

    /** Lets a tall form scroll on small screens, without a visible border or background. */
    static JScrollPane scrollable(JComponent component) {
        JScrollPane scroll = new JScrollPane(topAligned(component),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_STEP);
        return scroll;
    }

    /** Runs an action every time the text in a field changes, for example to update a live estimate. */
    static void onTextChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                action.run();
            }
        });
    }
}
