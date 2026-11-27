package expensetracker;

import java.util.Locale;

/** Small helper class for consistent text formatting across the program. */
public final class Fmt {
    public static final String CURRENCY = "PHP";

    private Fmt() { }

    public static String money(double value) {
        return String.format(Locale.US, "%s %,.2f", CURRENCY, value);
    }

    public static String signed(double value) {
        return String.format(Locale.US, "%+,.2f", value);
    }

    /** Text progress bar, e.g. bar(0.5, 10) -> "#####.....". */
    public static String bar(double fraction, int width) {
        if (Double.isNaN(fraction) || fraction < 0) {
            fraction = 0;
        }
        int filled = (int) Math.round(Math.min(fraction, 1.0) * width);
        return "#".repeat(filled) + ".".repeat(width - filled);
    }

    public static String line(char c, int length) {
        return String.valueOf(c).repeat(length);
    }

    public static String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }
}
