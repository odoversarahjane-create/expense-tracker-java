package expensetracker;

import java.time.YearMonth;

/** INTERFACE implemented by every report type (polymorphism). */
public interface Reportable {
    String getReportName();

    String generateReport(YearMonth month);
}
