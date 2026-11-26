package cwrm.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.util.Map;

/**
 * The look of the whole app in one place: the FlatLaf theme, a green "recycling" accent
 * colour and the colours used for cards, messages and pickup statuses.
 *
 * <p>Changing a colour here changes it on every screen.
 */
public final class Theme {
    /** Main green, used for the header, primary buttons and bars. */
    public static final Color PRIMARY = new Color(0x2E7D32);
    /** Darker green, used for success text and the header gradient. */
    public static final Color PRIMARY_DARK = new Color(0x1B5E20);
    /** Very light green, used behind success messages and completed badges. */
    public static final Color PRIMARY_LIGHT = new Color(0xE8F5E9);
    /** Amber text for pending pickups. */
    public static final Color PENDING = new Color(0x8A5300);
    /** Light amber behind pending badges. */
    public static final Color PENDING_LIGHT = new Color(0xFFEFD5);
    /** Grey text for cancelled pickups and hints. */
    public static final Color MUTED = new Color(0x5F6B66);
    /** Light grey behind cancelled badges and information messages. */
    public static final Color MUTED_LIGHT = new Color(0xECEFF1);
    /** Red text for problems. */
    public static final Color PROBLEM = new Color(0xB71C1C);
    /** Light red behind problem messages. */
    public static final Color PROBLEM_LIGHT = new Color(0xFDECEA);
    /** Blue used for the second chart and information accents. */
    public static final Color INFO = new Color(0x1565C0);
    /** Thin border around cards. */
    public static final Color CARD_BORDER = new Color(0xDCE3DE);
    /** Standard gap between parts of a screen, in pixels. */
    public static final int GAP = 12;
    /** Half the standard gap, for space inside a card. */
    public static final int SMALL_GAP = GAP / 2;

    private static final String ACCENT_HEX = "#2E7D32";
    private static final Map<String, String> FLATLAF_SETTINGS = Map.ofEntries(
            Map.entry("@accentColor", ACCENT_HEX),
            Map.entry("defaultFont", "+1"),
            Map.entry("Panel.background", "#F3F6F4"),
            Map.entry("Component.arc", "10"),
            Map.entry("Button.arc", "10"),
            Map.entry("TextComponent.arc", "10"),
            Map.entry("ScrollBar.thumbArc", "999"),
            Map.entry("Table.rowHeight", "30"),
            Map.entry("Table.showHorizontalLines", "true"),
            Map.entry("Table.alternateRowColor", "#F7FAF8"),
            Map.entry("TableHeader.height", "32"),
            Map.entry("TabbedPane.tabHeight", "40"),
            Map.entry("TabbedPane.selectedBackground", "#FFFFFF"));

    private Theme() {
        // Holds constants and one set-up method only; no instances.
    }

    /** Switches Swing to the FlatLaf light theme with the app's colours. Call once, before any window opens. */
    public static void install() {
        FlatLaf.setGlobalExtraDefaults(FLATLAF_SETTINGS);
        FlatLightLaf.setup();
    }
}
