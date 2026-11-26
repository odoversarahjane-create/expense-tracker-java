# Contribution Statements

**Project:** Expense Tracker – Mini Java Project, Topic #13
**Date:** November 2026

---

## Badilla, Reynald Rey (Project Leader)

**GitHub username:** [GitHub username]

As project leader, I created the GitHub repository, added my groupmates as
collaborators, and coordinated how we split the work. I designed the transaction
model: the abstract `Transaction` class with private fields and validated setters,
its subclasses `Expense` and `Income`, and our custom `InvalidTransactionException`.
I also wrote `ExpenseManager`, which stores transactions in an `ArrayList` and
budgets in a `HashMap`, and provides adding, deleting, searching, sorting and
monthly totals. Finally, I built the budget feature with `BudgetStatus`, including
the 80% "near limit" and over-budget warnings. This part taught me how inheritance
and polymorphism make the rest of the program simpler.

## Gumarang, Christian

**GitHub username:** istiandemigod27

I was responsible for file persistence and the reports. I wrote `FileManager`,
which saves and loads transactions and budgets as CSV files, handles descriptions
that contain commas or quotes, skips damaged lines instead of crashing, and exports
reports as text files. For the reports, I created the `Reportable` interface and
the `AbstractReport` class, which uses the Template Method pattern so every report
shares the same header and footer. I then wrote the three reports:
`MonthlySummaryReport`, `CategoryBreakdownReport` and `BudgetReport`. I also made
the `Fmt` helper class so that money, bars and tables are formatted the same way
throughout the program.

## Odover, Sarah Jane

**GitHub username:** odoversarahjane-create

I worked on the user interface, testing and documentation. I wrote `ConsoleUI`,
the menu-driven interface that validates every input so the program never crashes
on bad data, and `Main`, the program's entry point. I converted our tests to JUnit 5,
writing 34 tests that cover validation, totals, search, sorting, budgets, file
saving and loading, and the reports, and I set up the `test.bat` script to run
them. I also drew the UML class and use case diagrams and wrote the README, the
project documentation and these contribution statements, making sure they match
what the code actually does.
