package expensetracker;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Abstract base class for every money movement (Expense or Income).
 * Demonstrates ENCAPSULATION (private fields + validated setters)
 * and is the root of the INHERITANCE / POLYMORPHISM hierarchy.
 */
public abstract class Transaction {
    public static final double MAX_AMOUNT = 1_000_000_000.0;

    private final int id;
    private String description;
    private double amount;
    private LocalDate date;
    private String category;

    protected Transaction(int id, String description, double amount, LocalDate date, String category)
            throws InvalidTransactionException {
        this.id = id;
        setDescription(description);
        setAmount(amount);
        setDate(date);
        setCategory(category);
    }

    // ---------- getters ----------
    public int getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }

    // ---------- validated setters ----------
    public final void setDescription(String description) throws InvalidTransactionException {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidTransactionException("Description cannot be empty.");
        }
        String clean = description.trim().replaceAll("\\s+", " ");
        if (clean.length() > 60) {
            throw new InvalidTransactionException("Description is too long (max 60 characters).");
        }
        this.description = clean;
    }

    public final void setAmount(double amount) throws InvalidTransactionException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new InvalidTransactionException("Amount must be a number greater than zero.");
        }
        if (amount > MAX_AMOUNT) {
            throw new InvalidTransactionException("Amount is too large.");
        }
        this.amount = Math.round(amount * 100.0) / 100.0;
    }

    public final void setDate(LocalDate date) throws InvalidTransactionException {
        if (date == null) {
            throw new InvalidTransactionException("Date is required.");
        }
        this.date = date;
    }

    public final void setCategory(String category) throws InvalidTransactionException {
        this.category = normalizeCategory(category);
    }

    /** Trims, collapses spaces and applies "Capitalised" style so "food" == "FOOD" == "Food". */
    public static String normalizeCategory(String raw) throws InvalidTransactionException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new InvalidTransactionException("Category cannot be empty.");
        }
        String c = raw.trim().replaceAll("\\s+", " ");
        if (c.length() > 20) {
            throw new InvalidTransactionException("Category name is too long (max 20 characters).");
        }
        return c.substring(0, 1).toUpperCase() + c.substring(1).toLowerCase();
    }

    // ---------- polymorphic behaviour ----------
    /** "EXPENSE" or "INCOME". */
    public abstract String getType();

    /** Negative for expenses, positive for income. */
    public abstract double getSignedAmount();

    /** One CSV line: type,id,date,category,amount,description */
    public String toCsv() {
        return String.join(",",
                getType(),
                String.valueOf(id),
                date.toString(),
                FileManager.escapeCsv(category),
                String.format(Locale.US, "%.2f", amount),
                FileManager.escapeCsv(description));
    }

    @Override
    public String toString() {
        return String.format("#%d [%s] %s | %s | %s | %s",
                id, getType(), date, category, Fmt.money(amount), description);
    }
}
