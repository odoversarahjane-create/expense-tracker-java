package expensetracker;

import java.time.YearMonth;
import java.util.List;

public class MonthlySummaryReport extends AbstractReport {

    public MonthlySummaryReport(ExpenseManager manager) {
        super(manager);
    }

    @Override
    public String getReportName() {
        return "Monthly Summary";
    }

    @Override
    protected String buildBody(YearMonth month) {
        List<Transaction> list = manager.transactionsInMonth(month);
        if (list.isEmpty()) {
            return "  No transactions recorded for this month.\n";
        }
        double income = manager.totalIncome(month);
        double expenses = manager.totalExpenses(month);
        double net = income - expenses;

        Expense largest = null;
        int expenseCount = 0;
        for (Transaction t : list) {
            if (t instanceof Expense) {
                expenseCount++;
                if (largest == null || t.getAmount() > largest.getAmount()) {
                    largest = (Expense) t;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Total income    : %s%n", Fmt.money(income)));
        sb.append(String.format("  Total expenses  : %s%n", Fmt.money(expenses)));
        sb.append(String.format("  Net %-11s : %s%n", net >= 0 ? "(saved)" : "(deficit)", Fmt.money(net)));
        if (income > 0) {
            sb.append(String.format("  Savings rate    : %.1f%%%n", net / income * 100));
        }
        sb.append(String.format("  Transactions    : %d (%d expenses)%n", list.size(), expenseCount));
        if (largest != null) {
            sb.append(String.format("  Largest expense : %s - %s%n", Fmt.money(largest.getAmount()), largest.getDescription()));
        }
        sb.append(Fmt.line('-', WIDTH)).append('\n');
        sb.append("  Transactions this month:\n");
        for (Transaction t : list) {
            sb.append(String.format("   %s  %-14s %14s  %s%n",
                    t.getDate(), Fmt.truncate(t.getCategory(), 14), Fmt.signed(t.getSignedAmount()),
                    Fmt.truncate(t.getDescription(), 22)));
        }
        return sb.toString();
    }
}
