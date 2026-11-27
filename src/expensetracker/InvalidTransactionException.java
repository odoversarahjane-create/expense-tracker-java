package expensetracker;

/**
 * Custom checked exception thrown when a transaction, category or budget
 * contains invalid data (empty text, non-positive amount, missing date, ...).
 */
public class InvalidTransactionException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidTransactionException(String message) {
        super(message);
    }
}
