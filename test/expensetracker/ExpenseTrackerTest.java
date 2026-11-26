package expensetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * JUnit 5 test suite for the Expense Tracker (34 tests).
 * Run with test.bat / test.sh (uses lib/junit-platform-console-standalone-1.10.2.jar).
 */
@DisplayName("Expense Tracker")
class ExpenseTrackerTest {

    private static final double EPS = 0.001;
    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_3 = LocalDate.of(2026, 11, 3);
    private static final YearMonth NOV = YearMonth.of(2026, 11);

    // =====================================================================
    @Nested
    @DisplayName("Validation (custom exception)")
    class Validation {
        private final ExpenseManager m = new ExpenseManager();

        @Test
        @DisplayName("rejects zero amount")
        void rejectsZeroAmount() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 0, NOV_1, "Food"));
        }

        @Test
        @DisplayName("rejects negative amount")
        void rejectsNegativeAmount() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", -5, NOV_1, "Food"));
        }

        @Test
        @DisplayName("rejects empty description")
        void rejectsEmptyDescription() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("  ", 5, NOV_1, "Food"));
        }

        @Test
        @DisplayName("rejects empty category")
        void rejectsEmptyCategory() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 5, NOV_1, ""));
        }

        @Test
        @DisplayName("rejects null date")
        void rejectsNullDate() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 5, null, "Food"));
        }

        @Test
        @DisplayName("failed adds do not store anything")
        void failedAddsDoNotStoreAnything() {
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 0, NOV_1, "Food"));
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", -5, NOV_1, "Food"));
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("  ", 5, NOV_1, "Food"));
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 5, NOV_1, ""));
            assertThrows(InvalidTransactionException.class, () -> m.addExpense("x", 5, null, "Food"));
            assertEquals(0, m.size());
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Polymorphism and totals")
    class PolymorphismAndTotals {
        private ExpenseManager m;
        private Transaction lunch;

        PolymorphismAndTotals() throws InvalidTransactionException {
            m = new ExpenseManager();
            m.addIncome("Salary", 1000, NOV_3, "salary");
            m.addExpense("Lunch", 150.505, NOV_3, "FOOD");
            lunch = m.getAll().get(1);
        }

        @Test
        @DisplayName("category is normalised")
        void categoryIsNormalised() {
            assertEquals("Food", lunch.getCategory());
        }

        @Test
        @DisplayName("amount rounded to 2 decimals")
        void amountRounded() {
            assertEquals(150.51, lunch.getAmount());
        }

        @Test
        @DisplayName("expense signed amount negative")
        void expenseSignedAmountNegative() {
            assertTrue(lunch.getSignedAmount() < 0);
        }

        @Test
        @DisplayName("balance uses polymorphism")
        void balanceUsesPolymorphism() {
            assertEquals(849.49, m.getBalance(), EPS);
        }

        @Test
        @DisplayName("monthly income total")
        void monthlyIncomeTotal() {
            assertEquals(1000.0, m.totalIncome(NOV));
        }

        @Test
        @DisplayName("monthly expense total")
        void monthlyExpenseTotal() {
            assertEquals(150.51, m.totalExpenses(NOV), EPS);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Search and sort")
    class SearchAndSort {
        private ExpenseManager m;

        SearchAndSort() throws InvalidTransactionException {
            m = new ExpenseManager();
            m.addExpense("Jeepney fare", 40, LocalDate.of(2026, 11, 2), "Transport");
            m.addExpense("Groceries", 900, LocalDate.of(2026, 11, 5), "Food");
            m.addExpense("Pizza night", 600, LocalDate.of(2026, 10, 30), "Food");
        }

        @Test
        @DisplayName("search keyword (case-insensitive)")
        void searchKeyword() {
            assertEquals(1, m.searchByKeyword("PIZZA").size());
        }

        @Test
        @DisplayName("search category")
        void searchCategory() {
            assertEquals(2, m.searchByCategory("food").size());
        }

        @Test
        @DisplayName("search date range")
        void searchDateRange() {
            assertEquals(2, m.searchByDateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)).size());
        }

        @Test
        @DisplayName("sort amount descending")
        void sortAmountDescending() {
            List<Transaction> byAmountDesc = m.sortBy(ExpenseManager.SortKey.AMOUNT, false);
            assertEquals(900.0, byAmountDesc.get(0).getAmount());
        }

        @Test
        @DisplayName("sort date ascending")
        void sortDateAscending() {
            List<Transaction> byDateAsc = m.sortBy(ExpenseManager.SortKey.DATE, true);
            assertEquals("Pizza night", byDateAsc.get(0).getDescription());
        }

        @Test
        @DisplayName("sort category")
        void sortCategory() {
            assertEquals("Food", m.sortBy(ExpenseManager.SortKey.CATEGORY, true).get(0).getCategory());
        }

        @Test
        @DisplayName("remove by id")
        void removeById() {
            assertTrue(m.removeById(1));
            assertEquals(2, m.size());
        }

        @Test
        @DisplayName("remove unknown id returns false")
        void removeUnknownId() {
            assertFalse(m.removeById(99));
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Budgets")
    class Budgets {
        private ExpenseManager m;

        Budgets() throws InvalidTransactionException {
            m = new ExpenseManager();
            m.setBudget("food", 1000);
        }

        @Test
        @DisplayName("near-limit warning at 85%")
        void nearLimitWarning() throws InvalidTransactionException {
            Expense e1 = m.addExpense("Groceries", 850, NOV_3, "Food");
            String warning = m.checkBudgetWarning(e1);
            assertNotNull(warning);
            assertFalse(warning.startsWith("WARNING"));
        }

        @Test
        @DisplayName("exceeded warning")
        void exceededWarning() throws InvalidTransactionException {
            m.addExpense("Groceries", 850, NOV_3, "Food");
            Expense e2 = m.addExpense("Party", 300, NOV_3, "Food");
            assertTrue(m.checkBudgetWarning(e2).startsWith("WARNING"));
        }

        @Test
        @DisplayName("budget status exceeded")
        void budgetStatusExceeded() throws InvalidTransactionException {
            m.addExpense("Groceries", 850, NOV_3, "Food");
            m.addExpense("Party", 300, NOV_3, "Food");
            BudgetStatus s = m.getBudgetStatus("Food", NOV);
            assertTrue(s.isExceeded());
            assertEquals("OVER BUDGET", s.getLabel());
        }

        @Test
        @DisplayName("no budget -> no warning")
        void noBudgetNoWarning() throws InvalidTransactionException {
            assertNull(m.checkBudgetWarning(m.addExpense("Bus", 10, NOV_3, "Transport")));
        }

        @Test
        @DisplayName("invalid budget rejected")
        void invalidBudgetRejected() {
            assertThrows(InvalidTransactionException.class, () -> m.setBudget("Food", -1));
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("File persistence")
    class Persistence {
        @TempDir
        Path dir;

        /** Saves two transactions and one budget to the temp folder and returns the FileManager used. */
        private FileManager saveSample() throws IOException, InvalidTransactionException {
            ExpenseManager m = new ExpenseManager();
            m.addExpense("Pizza, \"large\" size", 499.99, NOV_3, "Food");
            m.addIncome("Allowance", 2000, NOV_3, "Allowance");
            m.setBudget("Food", 3000);
            FileManager fm = new FileManager(dir.toString());
            fm.save(m);
            return fm;
        }

        /** Saves the sample data, then loads it into a fresh manager. */
        private ExpenseManager saveAndReload() throws IOException, InvalidTransactionException {
            ExpenseManager loaded = new ExpenseManager();
            saveSample().load(loaded);
            return loaded;
        }

        @Test
        @DisplayName("load returns count")
        void loadReturnsCount() throws Exception {
            assertEquals(2, saveSample().load(new ExpenseManager()));
        }

        @Test
        @DisplayName("description with comma/quotes survives")
        void descriptionWithCommaAndQuotesSurvives() throws Exception {
            assertEquals("Pizza, \"large\" size", saveAndReload().findById(1).getDescription());
        }

        @Test
        @DisplayName("budgets survive")
        void budgetsSurvive() throws Exception {
            assertEquals(3000.0, saveAndReload().getBudgets().get("Food"));
        }

        @Test
        @DisplayName("ids continue after load")
        void idsContinueAfterLoad() throws Exception {
            assertEquals(3, saveAndReload().addExpense("Next", 1, NOV_3, "Food").getId());
        }

        @Test
        @DisplayName("corrupt line skipped, good line loaded")
        void corruptLineSkipped() throws Exception {
            Files.write(dir.resolve("transactions.csv"),
                    ("type,id,date,category,amount,description\n"
                            + "EXPENSE,1,not-a-date,Food,5,Bad\n"
                            + "EXPENSE,2,2026-11-03,Food,5,Good\n").getBytes(StandardCharsets.UTF_8));
            FileManager fm = new FileManager(dir.toString());
            ExpenseManager corrupt = new ExpenseManager();
            assertEquals(1, fm.load(corrupt));
            assertTrue(fm.getLastSkipped() >= 1);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Reports")
    class Reports {
        private ExpenseManager m;

        Reports() throws InvalidTransactionException {
            m = new ExpenseManager();
            m.addIncome("Salary", 1000, NOV_3, "Salary");
            m.addExpense("Food run", 200, NOV_3, "Food");
            m.setBudget("Food", 300);
        }

        private void assertGenerates(Reportable r) {
            String text = r.generateReport(NOV);
            assertTrue(text.contains("November 2026"), r.getReportName() + " should show the period");
            assertTrue(text.length() > 100, r.getReportName() + " should have a body");
        }

        @Test
        @DisplayName("Monthly Summary generates")
        void monthlySummaryGenerates() {
            assertGenerates(new MonthlySummaryReport(m));
        }

        @Test
        @DisplayName("Spending by Category generates")
        void categoryBreakdownGenerates() {
            assertGenerates(new CategoryBreakdownReport(m));
        }

        @Test
        @DisplayName("Budget Status generates")
        void budgetReportGenerates() {
            assertGenerates(new BudgetReport(m));
        }

        @Test
        @DisplayName("empty month handled")
        void emptyMonthHandled() {
            assertTrue(new MonthlySummaryReport(m).generateReport(YearMonth.of(2020, 1)).contains("No transactions"));
        }
    }
}
