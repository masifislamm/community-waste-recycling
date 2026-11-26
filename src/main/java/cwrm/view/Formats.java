package cwrm.view;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Turns numbers, dates and durations into the short, readable text shown on screen,
 * so every screen writes them the same way.
 */
final class Formats {
    private static final String KG = "%.1f kg";
    private static final String PERCENT = "%.0f %%";
    private static final String POINTS = "%d pts";
    private static final String HOURS_AND_MINUTES = "%d h %02d min";
    private static final String UNDER_A_MINUTE = "Under 1 min";
    private static final String NONE_YET = "None yet";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH);

    private Formats() {
        // Static helpers only; no instances.
    }

    /** For example {@code 12.5 kg}. */
    static String kg(double kg) {
        return String.format(KG, kg);
    }

    /** For example {@code 75 %}. */
    static String percent(double percent) {
        return String.format(PERCENT, percent);
    }

    /** For example {@code 40 pts}. */
    static String points(long points) {
        return String.format(POINTS, points);
    }

    /** For example {@code Fri, 27 Nov 2026}. */
    static String day(LocalDate date) {
        return DAY.format(date);
    }

    /** For example {@code Fri 27 Nov}, for narrow table columns. */
    static String shortDay(LocalDate date) {
        return SHORT_DAY.format(date);
    }

    /** For example {@code 27 Nov, 14:05}. */
    static String time(LocalDateTime time) {
        return TIME.format(time);
    }

    /** For example {@code 2 h 05 min}, or a friendly phrase for very short or empty durations. */
    static String turnaround(Duration duration, boolean anyCompleted) {
        if (!anyCompleted) {
            return NONE_YET;
        }
        if (duration.toMinutes() < 1) {
            return UNDER_A_MINUTE;
        }
        return String.format(HOURS_AND_MINUTES, duration.toHours(), duration.toMinutesPart());
    }
}
