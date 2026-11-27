package expensetracker;

import java.time.LocalDate;

/** Money coming IN (salary, allowance, ...). Subclass of Transaction. */
public class Income extends Transaction {

    public Income(int id, String description, double amount, LocalDate date, String category)
            throws InvalidTransactionException {
        super(id, description, amount, date, category);
    }

    @Override
    public String getType() {
        return "INCOME";
    }

    @Override
    public double getSignedAmount() {
        return getAmount();
    }
}
