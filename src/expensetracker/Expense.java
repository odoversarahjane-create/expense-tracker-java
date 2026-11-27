package expensetracker;

import java.time.LocalDate;

/** Money going OUT. Subclass of Transaction. */
public class Expense extends Transaction {

    public Expense(int id, String description, double amount, LocalDate date, String category)
            throws InvalidTransactionException {
        super(id, description, amount, date, category);
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }

    @Override
    public double getSignedAmount() {
        return -getAmount();
    }
}
