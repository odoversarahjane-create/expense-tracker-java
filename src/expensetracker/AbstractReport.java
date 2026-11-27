package expensetracker;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * ABSTRACT CLASS using the Template Method pattern:
 * generateReport() builds the common header, subclasses supply the body.
 */
public abstract class AbstractReport implements Reportable {
    protected static final int WIDTH = 64;
    protected final ExpenseManager manager;

    protected AbstractReport(ExpenseManager manager) {
        this.manager = manager;
    }

    @Override
    public final String generateReport(YearMonth month) {
        StringBuilder sb = new StringBuilder();
        sb.append(Fmt.line('=', WIDTH)).append('\n');
        sb.append("  EXPENSE TRACKER - ").append(getReportName().toUpperCase()).append('\n');
        sb.append("  Period: ")
          .append(month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
          .append(' ').append(month.getYear()).append('\n');
        sb.append(Fmt.line('=', WIDTH)).append('\n');
        sb.append(buildBody(month));
        sb.append(Fmt.line('=', WIDTH)).append('\n');
        return sb.toString();
    }

    /** Subclasses return the report body text. */
    protected abstract String buildBody(YearMonth month);
}
