package expensetracker;

/** Immutable snapshot of one category budget for one month. */
public class BudgetStatus {
    private final String category;
    private final double limit;
    private final double spent;

    public BudgetStatus(String category, double limit, double spent) {
        this.category = category;
        this.limit = limit;
        this.spent = spent;
    }

    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public double getSpent() { return spent; }
    public double getRemaining() { return limit - spent; }
    public double getFractionUsed() { return limit == 0 ? 0 : spent / limit; }
    public boolean isExceeded() { return spent > limit; }
    public boolean isNearLimit() { return !isExceeded() && getFractionUsed() >= 0.8; }

    public String getLabel() {
        if (isExceeded()) return "OVER BUDGET";
        if (isNearLimit()) return "NEAR LIMIT";
        return "OK";
    }
}
