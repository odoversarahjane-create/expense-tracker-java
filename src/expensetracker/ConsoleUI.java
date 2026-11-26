package expensetracker;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Menu-driven console interface. All user input is validated; the program never crashes on bad input. */
public class ConsoleUI {
    private static final String[] EXPENSE_CATEGORIES =
            {"Food", "Transport", "Bills", "Shopping", "Health", "Entertainment", "Education", "Other"};
    private static final String[] INCOME_CATEGORIES =
            {"Salary", "Allowance", "Business", "Gift", "Other"};

    private final Scanner in = new Scanner(System.in);
    private final ExpenseManager manager = new ExpenseManager();
    private final FileManager files;
    private final List<Reportable> reports = new ArrayList<>();

    public ConsoleUI(String dataDir) {
        this.files = new FileManager(dataDir);
        reports.add(new MonthlySummaryReport(manager));
        reports.add(new CategoryBreakdownReport(manager));
        reports.add(new BudgetReport(manager));
    }

    // =====================================================================
    // Main loop
    // =====================================================================
    public void run() {
        printBanner();
        loadData();
        try {
            boolean running = true;
            while (running) {
                printMenu();
                switch (readInt("Choose an option", 0, 9)) {
                    case 1: addTransaction(true); break;
                    case 2: addTransaction(false); break;
                    case 3: viewAll(); break;
                    case 4: searchMenu(); break;
                    case 5: sortMenu(); break;
                    case 6: budgetMenu(); break;
                    case 7: reportMenu(); break;
                    case 8: deleteTransaction(); break;
                    case 9: loadDemoData(); break;
                    default: running = false; break;
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\n(Input closed)");
        }
        saveData();
        System.out.println("\nData saved to '" + files.getDirectory() + "'. Goodbye!");
    }

    private void printBanner() {
        System.out.println(Fmt.line('=', 50));
        System.out.println("        EXPENSE TRACKER  -  Java OOP Project");
        System.out.println(Fmt.line('=', 50));
    }

    private void printMenu() {
        YearMonth now = YearMonth.now();
        System.out.println();
        System.out.println(Fmt.line('-', 50));
        System.out.printf(" Balance: %s%n", Fmt.money(manager.getBalance()));
        System.out.printf(" This month  income %s | expenses %s%n",
                Fmt.money(manager.totalIncome(now)), Fmt.money(manager.totalExpenses(now)));
        System.out.println(Fmt.line('-', 50));
        System.out.println(" 1. Add expense");
        System.out.println(" 2. Add income");
        System.out.println(" 3. View all transactions");
        System.out.println(" 4. Search transactions");
        System.out.println(" 5. Sort transactions");
        System.out.println(" 6. Manage monthly budgets");
        System.out.println(" 7. Reports & summaries");
        System.out.println(" 8. Delete a transaction");
        System.out.println(" 9. Load demo data");
        System.out.println(" 0. Save and exit");
    }

    // =====================================================================
    // Menu actions
    // =====================================================================
    private void addTransaction(boolean isExpense) {
        System.out.println(isExpense ? "\n--- Add Expense ---" : "\n--- Add Income ---");
        String description = readNonEmpty("Description");
        double amount = readAmount("Amount (" + Fmt.CURRENCY + ")");
        LocalDate date = readDate("Date (yyyy-MM-dd, Enter = today)");
        String category = chooseCategory(isExpense ? EXPENSE_CATEGORIES : INCOME_CATEGORIES);
        try {
            if (isExpense) {
                Expense e = manager.addExpense(description, amount, date, category);
                System.out.println("Added: " + e);
                String warning = manager.checkBudgetWarning(e);
                if (warning != null) {
                    System.out.println("  >> " + warning);
                }
            } else {
                System.out.println("Added: " + manager.addIncome(description, amount, date, category));
            }
            autoSave();
        } catch (InvalidTransactionException ex) {
            System.out.println("Could not add transaction: " + ex.getMessage());
        }
    }

    private void viewAll() {
        System.out.println("\n--- All Transactions (newest first) ---");
        printTable(manager.sortBy(ExpenseManager.SortKey.DATE, false));
    }

    private void searchMenu() {
        System.out.println("\n--- Search ---");
        System.out.println(" 1. By keyword (description or category)");
        System.out.println(" 2. By category");
        System.out.println(" 3. By date range");
        System.out.println(" 4. By ID");
        System.out.println(" 0. Back");
        switch (readInt("Choose", 0, 4)) {
            case 1:
                printTable(manager.searchByKeyword(readNonEmpty("Keyword")));
                break;
            case 2:
                printTable(manager.searchByCategory(readNonEmpty("Category")));
                break;
            case 3:
                LocalDate from = readDate("From (yyyy-MM-dd, Enter = today)");
                LocalDate to = readDate("To   (yyyy-MM-dd, Enter = today)");
                if (from.isAfter(to)) {
                    LocalDate tmp = from;
                    from = to;
                    to = tmp;
                    System.out.println("(dates were swapped so the range is valid)");
                }
                printTable(manager.searchByDateRange(from, to));
                break;
            case 4:
                Transaction t = manager.findById(readInt("Transaction ID", 1, Integer.MAX_VALUE));
                if (t == null) {
                    System.out.println("No transaction with that ID.");
                } else {
                    System.out.println(t);
                }
                break;
            default:
                break;
        }
    }

    private void sortMenu() {
        System.out.println("\n--- Sort ---");
        System.out.println(" 1. By date");
        System.out.println(" 2. By amount");
        System.out.println(" 3. By category");
        System.out.println(" 0. Back");
        int choice = readInt("Sort by", 0, 3);
        if (choice == 0) {
            return;
        }
        System.out.println(" 1. Ascending (smallest/oldest first)");
        System.out.println(" 2. Descending (largest/newest first)");
        boolean ascending = readInt("Order", 1, 2) == 1;
        ExpenseManager.SortKey key = ExpenseManager.SortKey.values()[choice - 1];
        printTable(manager.sortBy(key, ascending));
    }

    private void budgetMenu() {
        System.out.println("\n--- Monthly Budgets ---");
        System.out.println(" 1. Set / update a category budget");
        System.out.println(" 2. Remove a budget");
        System.out.println(" 3. View budget status");
        System.out.println(" 0. Back");
        switch (readInt("Choose", 0, 3)) {
            case 1:
                String category = chooseCategory(EXPENSE_CATEGORIES);
                double limit = readAmount("Monthly limit (" + Fmt.CURRENCY + ")");
                try {
                    manager.setBudget(category, limit);
                    System.out.println("Budget saved for '" + category + "': " + Fmt.money(limit));
                    autoSave();
                } catch (InvalidTransactionException e) {
                    System.out.println("Could not set budget: " + e.getMessage());
                }
                break;
            case 2:
                if (manager.removeBudget(readNonEmpty("Category to remove"))) {
                    System.out.println("Budget removed.");
                    autoSave();
                } else {
                    System.out.println("No budget found for that category.");
                }
                break;
            case 3:
                System.out.println(reports.get(2).generateReport(readMonth("Month (yyyy-MM, Enter = this month)")));
                break;
            default:
                break;
        }
    }

    private void reportMenu() {
        System.out.println("\n--- Reports ---");
        for (int i = 0; i < reports.size(); i++) {
            System.out.printf(" %d. %s%n", i + 1, reports.get(i).getReportName());
        }
        System.out.println(" 0. Back");
        int choice = readInt("Choose a report", 0, reports.size());
        if (choice == 0) {
            return;
        }
        Reportable report = reports.get(choice - 1);
        YearMonth month = readMonth("Month (yyyy-MM, Enter = this month)");
        String text = report.generateReport(month);
        System.out.println();
        System.out.println(text);

        if (readYesNo("Export this report to a text file?")) {
            String name = report.getReportName().toLowerCase().replace(' ', '_') + "_" + month + ".txt";
            try {
                Path p = files.exportReport(name, text);
                System.out.println("Report saved to " + p);
            } catch (IOException e) {
                System.out.println("Could not export report: " + e.getMessage());
            }
        }
    }

    private void deleteTransaction() {
        System.out.println("\n--- Delete Transaction ---");
        if (manager.isEmpty()) {
            System.out.println("Nothing to delete.");
            return;
        }
        Transaction t = manager.findById(readInt("Transaction ID", 1, Integer.MAX_VALUE));
        if (t == null) {
            System.out.println("No transaction with that ID.");
            return;
        }
        System.out.println(t);
        if (readYesNo("Delete this transaction?")) {
            manager.removeById(t.getId());
            System.out.println("Deleted.");
            autoSave();
        } else {
            System.out.println("Cancelled.");
        }
    }

    private void loadDemoData() {
        if (!manager.isEmpty() && !readYesNo("You already have data. Add demo data anyway?")) {
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            manager.addIncome("Monthly salary", 25000, today.minusDays(35), "Salary");
            manager.addIncome("Monthly salary", 25000, today.minusDays(5), "Salary");
            manager.addIncome("Freelance project", 4500, today.minusDays(12), "Business");
            manager.addExpense("Lunch at canteen", 120, today.minusDays(1), "Food");
            manager.addExpense("Milk tea", 95, today.minusDays(3), "Food");
            manager.addExpense("Groceries", 1850.50, today.minusDays(6), "Food");
            manager.addExpense("Dinner with family", 1200, today.minusDays(14), "Food");
            manager.addExpense("Jeepney fare", 40, today.minusDays(2), "Transport");
            manager.addExpense("Grab ride", 285, today.minusDays(9), "Transport");
            manager.addExpense("Beep card load", 500, today.minusDays(20), "Transport");
            manager.addExpense("Electricity bill", 2100, today.minusDays(8), "Bills");
            manager.addExpense("Internet bill", 1499, today.minusDays(16), "Bills");
            manager.addExpense("Mobile load", 300, today.minusDays(4), "Bills");
            manager.addExpense("T-shirt", 799, today.minusDays(7), "Shopping");
            manager.addExpense("Sneakers", 2500, today.minusDays(18), "Shopping");
            manager.addExpense("Movie ticket", 350, today.minusDays(10), "Entertainment");
            manager.addExpense("Streaming subscription", 549, today.minusDays(11), "Entertainment");
            manager.addExpense("Vitamins", 450, today.minusDays(13), "Health");
            manager.addExpense("Notebooks and pens", 380, today.minusDays(5), "Education");
            manager.addExpense("Online course", 1500, today.minusDays(22), "Education");
            manager.setBudget("Food", 4000);
            manager.setBudget("Transport", 1500);
            manager.setBudget("Bills", 4500);
            manager.setBudget("Shopping", 2500);
            manager.setBudget("Entertainment", 800);
            System.out.println("Demo data loaded (20 transactions, 5 budgets).");
            autoSave();
        } catch (InvalidTransactionException e) {
            System.out.println("Demo data error: " + e.getMessage());
        }
    }

    // =====================================================================
    // Display helper
    // =====================================================================
    private void printTable(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println(Fmt.line('-', 80));
        System.out.printf("%-4s %-8s %-11s %-14s %14s  %s%n", "ID", "Type", "Date", "Category", "Amount", "Description");
        System.out.println(Fmt.line('-', 80));
        double net = 0;
        for (Transaction t : list) {
            net += t.getSignedAmount();
            System.out.printf("%-4d %-8s %-11s %-14s %14s  %s%n",
                    t.getId(), t.getType(), t.getDate(), Fmt.truncate(t.getCategory(), 14),
                    Fmt.signed(t.getSignedAmount()), Fmt.truncate(t.getDescription(), 28));
        }
        System.out.println(Fmt.line('-', 80));
        System.out.printf("%d transaction(s) | Net: %s%n", list.size(), Fmt.money(net));
    }

    // =====================================================================
    // Persistence helpers
    // =====================================================================
    private void loadData() {
        try {
            int n = files.load(manager);
            if (n > 0) {
                System.out.println("Loaded " + n + " transaction(s) from '" + files.getDirectory() + "'.");
            } else {
                System.out.println("No saved data yet - tip: choose option 9 to load demo data.");
            }
            if (files.getLastSkipped() > 0) {
                System.out.println("Note: " + files.getLastSkipped() + " invalid line(s) in the data files were skipped.");
            }
        } catch (IOException e) {
            System.out.println("Could not read saved data: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            files.save(manager);
        } catch (IOException e) {
            System.out.println("WARNING: could not save data: " + e.getMessage());
        }
    }

    private void autoSave() {
        saveData();
    }

    // =====================================================================
    // Validated input helpers
    // =====================================================================
    private String readLine(String prompt) {
        System.out.print(prompt + ": ");
        return in.nextLine().trim();
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("  This cannot be empty.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // fall through to message
            }
            System.out.println("  Please enter a whole number from " + min + " to " + (max == Integer.MAX_VALUE ? "..." : max) + ".");
        }
    }

    private double readAmount(String prompt) {
        while (true) {
            String s = readLine(prompt).replace(",", "");
            try {
                double v = Double.parseDouble(s);
                if (v > 0 && v <= Transaction.MAX_AMOUNT) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // fall through to message
            }
            System.out.println("  Please enter a positive number (e.g. 250.75).");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date. Use the format yyyy-MM-dd (e.g. 2026-11-13).");
            }
        }
    }

    private YearMonth readMonth(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                return YearMonth.now();
            }
            try {
                return YearMonth.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid month. Use the format yyyy-MM (e.g. 2026-11).");
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n)").toLowerCase();
            if (s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }

    private String chooseCategory(String[] options) {
        StringBuilder sb = new StringBuilder("Categories: ");
        for (int i = 0; i < options.length; i++) {
            sb.append(i + 1).append(") ").append(options[i]).append("  ");
        }
        System.out.println(sb.toString().trim());
        while (true) {
            String s = readLine("Category (number, or type your own)");
            if (s.isEmpty()) {
                System.out.println("  This cannot be empty.");
                continue;
            }
            try {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= options.length) {
                    return options[n - 1];
                }
                System.out.println("  Choose a number from 1 to " + options.length + " or type a name.");
            } catch (NumberFormatException e) {
                return s;
            }
        }
    }
}
