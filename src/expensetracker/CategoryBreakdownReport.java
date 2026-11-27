package expensetracker;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Spending report: how much was spent per category, with a text bar chart. */
public class CategoryBreakdownReport extends AbstractReport {

    public CategoryBreakdownReport(ExpenseManager manager) {
        super(manager);
    }

    @Override
    public String getReportName() {
        return "Spending by Category";
    }

    @Override
    protected String buildBody(YearMonth month) {
        Map<String, Double> byCategory = manager.spendingByCategory(month);
        if (byCategory.isEmpty()) {
            return "  No expenses recorded for this month.\n";
        }
        double total = manager.totalExpenses(month);

        List<Map.Entry<String, Double>> entries = new ArrayList<>(byCategory.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));   // highest first

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : entries) {
            double share = e.getValue() / total;
            sb.append(String.format("  %-14s %14s %6.1f%%  %s%n",
                    Fmt.truncate(e.getKey(), 14), Fmt.money(e.getValue()), share * 100, Fmt.bar(share, 20)));
        }
        sb.append(Fmt.line('-', WIDTH)).append('\n');
        sb.append(String.format("  %-14s %14s%n", "TOTAL", Fmt.money(total)));
        return sb.toString();
    }
}
