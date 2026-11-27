package expensetracker;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Core business logic: stores transactions (ArrayList) and monthly budgets (HashMap),
 * and provides add / delete / SEARCH / SORT / summary operations.
 */
public class ExpenseManager {

    public enum SortKey { DATE, AMOUNT, CATEGORY }

    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Double> budgets = new HashMap<>();
    private int nextId = 1;

    // =====================================================================
    // Add / remove
    // =====================================================================
    public Expense addExpense(String description, double amount, LocalDate date, String category)
            throws InvalidTransactionException {
        Expense e = new Expense(nextId, description, amount, date, category);
        transactions.add(e);
        nextId++;
        return e;
    }

    public Income addIncome(String description, double amount, LocalDate date, String category)
            throws InvalidTransactionException {
        Income i = new Income(nextId, description, amount, date, category);
        transactions.add(i);
        nextId++;
        return i;
    }

    /** Used when loading from file: keeps the stored id. */
    public void restore(Transaction t) throws InvalidTransactionException {
        if (findById(t.getId()) != null) {
            throw new InvalidTransactionException("Duplicate transaction id " + t.getId());
        }
        transactions.add(t);
        nextId = Math.max(nextId, t.getId() + 1);
    }

    public boolean removeById(int id) {
        Transaction t = findById(id);
        return t != null && transactions.remove(t);
    }

    public Transaction findById(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Transaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    public int size() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    // =====================================================================
    // SEARCH features
    // =====================================================================
    public List<Transaction> searchByKeyword(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getDescription().toLowerCase().contains(k) || t.getCategory().toLowerCase().contains(k)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> searchByCategory(String category) {
        String c = category == null ? "" : category.trim();
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getCategory().equalsIgnoreCase(c)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> searchByDateRange(LocalDate from, LocalDate to) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(from) && !t.getDate().isAfter(to)) {
                result.add(t);
            }
        }
        return result;
    }

    // =====================================================================
    // SORT feature
    // =====================================================================
    public List<Transaction> sortBy(SortKey key, boolean ascending) {
        Comparator<Transaction> cmp;
        switch (key) {
            case AMOUNT:
                cmp = Comparator.comparingDouble(Transaction::getAmount);
                break;
            case CATEGORY:
                cmp = Comparator.comparing(Transaction::getCategory, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                cmp = Comparator.comparing(Transaction::getDate);
                break;
        }
        cmp = cmp.thenComparingInt(Transaction::getId);
        if (!ascending) {
            cmp = cmp.reversed();
        }
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(cmp);
        return copy;
    }

    // =====================================================================
    // Budgets (category -> monthly limit)
    // =====================================================================
    public void setBudget(String category, double limit) throws InvalidTransactionException {
        String c = Transaction.normalizeCategory(category);
        if (Double.isNaN(limit) || limit <= 0 || limit > Transaction.MAX_AMOUNT) {
            throw new InvalidTransactionException("Budget must be greater than zero.");
        }
        budgets.put(c, Math.round(limit * 100.0) / 100.0);
    }

    public boolean removeBudget(String category) {
        try {
            return budgets.remove(Transaction.normalizeCategory(category)) != null;
        } catch (InvalidTransactionException e) {
            return false;
        }
    }

    /** Sorted copy of all budgets. */
    public Map<String, Double> getBudgets() {
        return new TreeMap<>(budgets);
    }

    public BudgetStatus getBudgetStatus(String category, YearMonth month) {
        Double limit = budgets.get(category);
        if (limit == null) {
            return null;
        }
        double spent = spendingByCategory(month).getOrDefault(category, 0.0);
        return new BudgetStatus(category, limit, spent);
    }

    public List<BudgetStatus> getBudgetStatuses(YearMonth month) {
        List<BudgetStatus> list = new ArrayList<>();
        for (String category : getBudgets().keySet()) {
            list.add(getBudgetStatus(category, month));
        }
        return list;
    }

    /** Returns a warning message if this expense pushed its category near/over budget, else null. */
    public String checkBudgetWarning(Expense e) {
        YearMonth month = YearMonth.from(e.getDate());
        BudgetStatus s = getBudgetStatus(e.getCategory(), month);
        if (s == null) {
            return null;
        }
        if (s.isExceeded()) {
            return String.format("WARNING: '%s' budget for %s exceeded by %s!",
                    s.getCategory(), month, Fmt.money(-s.getRemaining()));
        }
        if (s.isNearLimit()) {
            return String.format("Heads up: %.0f%% of your '%s' budget for %s is used (%s left).",
                    s.getFractionUsed() * 100, s.getCategory(), month, Fmt.money(s.getRemaining()));
        }
        return null;
    }

    // =====================================================================
    // Summaries
    // =====================================================================
    public List<Transaction> transactionsInMonth(YearMonth month) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(month)) {
                list.add(t);
            }
        }
        list.sort(Comparator.comparing(Transaction::getDate).thenComparingInt(Transaction::getId));
        return list;
    }

    public double totalIncome(YearMonth month) {
        double sum = 0;
        for (Transaction t : transactionsInMonth(month)) {
            if (t instanceof Income) {
                sum += t.getAmount();
            }
        }
        return sum;
    }

    public double totalExpenses(YearMonth month) {
        double sum = 0;
        for (Transaction t : transactionsInMonth(month)) {
            if (t instanceof Expense) {
                sum += t.getAmount();
            }
        }
        return sum;
    }

    /** Overall balance across all time (income - expenses). */
    public double getBalance() {
        double sum = 0;
        for (Transaction t : transactions) {
            sum += t.getSignedAmount();   // polymorphic call
        }
        return sum;
    }

    public Map<String, Double> spendingByCategory(YearMonth month) {
        Map<String, Double> map = new TreeMap<>();
        for (Transaction t : transactionsInMonth(month)) {
            if (t instanceof Expense) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }
}
