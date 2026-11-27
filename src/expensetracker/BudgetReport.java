package expensetracker;

import java.time.YearMonth;
import java.util.List;

/** Compares actual spending with each monthly category budget. */
public class BudgetReport extends AbstractReport {

    public BudgetReport(ExpenseManager manager) {
        super(manager);
    }

    @Override
    public String getReportName() {
        return "Budget Status";
    }

    @Override
    protected String buildBody(YearMonth month) {
        List<BudgetStatus> statuses = manager.getBudgetStatuses(month);
        if (statuses.isEmpty()) {
            return "  No budgets set yet. Use 'Manage monthly budgets' to create one.\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-13s %13s %13s %13s%n", "Category", "Budget", "Spent", "Remaining"));
        sb.append(Fmt.line('-', WIDTH)).append('\n');
        for (BudgetStatus s : statuses) {
            sb.append(String.format("  %-13s %13s %13s %13s%n",
                    Fmt.truncate(s.getCategory(), 13),
                    String.format("%,.2f", s.getLimit()),
                    String.format("%,.2f", s.getSpent()),
                    String.format("%,.2f", s.getRemaining())));
            sb.append(String.format("    [%s] %5.1f%%  %s%n",
                    Fmt.bar(s.getFractionUsed(), 25), s.getFractionUsed() * 100, s.getLabel()));
        }
        return sb.toString();
    }
}
