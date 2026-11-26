# Expense Tracker (Java OOP Project)

**Mini Java Project – Topic #13: Expense Tracker**

**Group members**
- Badilla, Reynald Rey (Project Leader)
- Gumarang, Christian
- Odover, Sarah Jane

A menu-driven console application that records income and expenses, enforces
monthly category budgets, and produces searchable, sortable data and reports.
Data is saved automatically to CSV files, so nothing is lost between runs.

## Features
- Add **expenses** and **income** (validated amount, date, category, description)
- **Categories** with quick-pick list, or type your own
- **Monthly budgets** per category with live warnings (80% = near limit, >100% = over budget)
- **Search** by keyword, category, date range, or ID
- **Sort** by date, amount, or category (ascending / descending)
- **Reports**: Monthly Summary, Spending by Category (text bar chart), Budget Status
- **Export** any report to a `.txt` file
- **Persistence**: `data/transactions.csv` and `data/budgets.csv` (auto-save + auto-load)
- **Demo data** option for presentations
- Robust input handling: bad input never crashes the program

## How to run
Requires **JDK 11 or newer** (`java -version` / `javac -version`).

Linux / macOS:
```
./build.sh
./run.sh
```
Windows (Command Prompt; in PowerShell type `.\build.bat` and `.\run.bat`):
```
build.bat
run.bat
```
Or manually:
```
javac -d out src/expensetracker/*.java
java -cp out expensetracker.Main
```
Optional: `java -cp out expensetracker.Main <data-folder>` to use a different data folder.

Building and running the app needs only the JDK; JUnit is used by the tests only.

## Tests (JUnit 5)
The test suite in `test/expensetracker/ExpenseTrackerTest.java` has **34 JUnit 5 tests**
(`@Test`, `@Nested`, `assertEquals`, `assertThrows`, and `@TempDir` for the file tests, so
your real `data/` folder is never touched).

```
test.bat        (Windows)
./test.sh       (Linux / macOS)
```
The scripts build the app, compile the tests against
`lib/junit-platform-console-standalone-1.10.2.jar`, and run them with the JUnit console
launcher. Expected result: `34 tests successful, 0 tests failed`.

If the jar is missing, download it from Maven Central:
https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/

## Project structure
```
src/expensetracker/
  Main.java                    entry point
  ConsoleUI.java               menus + validated input
  ExpenseManager.java          business logic (add, delete, search, sort, budgets, totals)
  Transaction.java             abstract base class (encapsulation + validation)
  Expense.java / Income.java   subclasses (inheritance + polymorphism)
  BudgetStatus.java            budget snapshot (spent / remaining / label)
  FileManager.java             CSV save / load, report export
  Reportable.java              interface
  AbstractReport.java          abstract class (template method)
  MonthlySummaryReport.java    \
  CategoryBreakdownReport.java  } report implementations
  BudgetReport.java            /
  InvalidTransactionException.java   custom exception
  Fmt.java                     formatting helpers
test/expensetracker/ExpenseTrackerTest.java   34 JUnit 5 tests
lib/junit-platform-console-standalone-1.10.2.jar   JUnit 5 (tests only)
docs/                          UML diagrams, project documentation,
                               contribution statements, Git guide
```

## Requirement checklist
| Requirement | Where |
|---|---|
| 5+ classes, clear responsibilities | 14 classes (see structure) |
| Inheritance + polymorphism, abstract class / interface | `Transaction` -> `Expense`/`Income`; `Reportable`, `AbstractReport` |
| Encapsulation | private fields, validated setters in `Transaction` |
| Collections | `ArrayList` (transactions), `HashMap` (budgets), `TreeMap` (sorted summaries) |
| Exception handling + custom exception | `InvalidTransactionException`, try/catch in UI and file I/O |
| File persistence | CSV via `FileManager` |
| Search + sort/report | `searchBy*()`, `sortBy()`, 3 reports |
| Version control | see `docs/GIT_GUIDE.md` |
| Bonus: unit testing | 34 JUnit 5 tests (`test.bat` / `test.sh`) |

Full write-up: `docs/Project_Documentation.md`. Individual contributions: `docs/Contribution_Statements.md`.

## Known limitations
- Console interface only (no GUI); single user; one currency (PHP, change `Fmt.CURRENCY`)
- No edit-in-place (delete and re-add instead)
- Budgets are monthly per category and do not roll over
