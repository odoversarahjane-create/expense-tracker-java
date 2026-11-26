# Git / GitHub guide (every member must have commits)

All commands below are for **Windows PowerShell**. Git commands themselves are the
same on every system; the differences are noted where they matter (running `.bat`
files, creating folders, etc.).

> **PowerShell tip:** PowerShell does not run scripts from the current folder by
> name alone. Type `.\build.bat`, `.\run.bat`, `.\test.bat` (with the leading `.\`).

## One-time setup (every member, on your own PC)
```powershell
git --version                                   # check Git is installed
git config --global user.name  "Your Full Name"
git config --global user.email "the-email-you-use-on-github@example.com"
```
The email must match your GitHub account, otherwise your commits will not be
linked to your profile and will not count as yours.

## Creating the repository (project leader: Badilla, Reynald Rey)
1. Create an empty repo on github.com (e.g. `expense-tracker-java`). Do **not** add a
   README or .gitignore on GitHub (we already have them).
2. Go to **Settings → Collaborators** and add Gumarang and Odover by GitHub username.
3. In PowerShell, inside the project folder:
```powershell
cd $HOME\Desktop\ExpenseTracker
git init
git branch -M main
git add .gitignore build.bat build.sh run.bat run.sh
git add src/expensetracker/Transaction.java src/expensetracker/Expense.java src/expensetracker/Income.java
git add src/expensetracker/InvalidTransactionException.java src/expensetracker/ExpenseManager.java src/expensetracker/BudgetStatus.java
git commit -m "Add Transaction hierarchy and custom exception"
git remote add origin https://github.com/<leader-username>/expense-tracker-java.git
git push -u origin main
```

## Every other member
```powershell
cd $HOME\Desktop
git clone https://github.com/<leader-username>/expense-tracker-java.git
cd expense-tracker-java
git checkout -b feature/<your-task>
# ...copy in / edit your files...
git status                                      # see what changed
git add <your files>
git commit -m "<what you did>"
git push -u origin feature/<your-task>
```
Then open a **Pull Request** on GitHub and merge it into `main` (or push straight
to `main` if your teacher allows).

Before starting new work, always get the latest version first:
```powershell
git checkout main
git pull origin main
git checkout -b feature/<next-task>
```

## Suggested split of work (one commit set per member)

| Member | Role | Files |
|---|---|---|
| **Badilla, Reynald Rey** | Project leader; creates the GitHub repo | `Transaction`, `Expense`, `Income`, `InvalidTransactionException`, `ExpenseManager`, `BudgetStatus` |
| **Gumarang, Christian** | Persistence and reports | `FileManager`, `Reportable`, `AbstractReport`, `MonthlySummaryReport`, `CategoryBreakdownReport`, `BudgetReport`, `Fmt` |
| **Odover, Sarah Jane** | User interface, testing and documentation | `ConsoleUI`, `Main`, JUnit tests (`test/`, `lib/`, `test.bat`, `test.sh`), UML diagrams, `README.md`, `docs/` |

### Example commit messages

**Badilla, Reynald Rey**
- `Add Transaction hierarchy and custom exception`
- `Implement ExpenseManager add, delete, search and sort`
- `Add monthly budgets with BudgetStatus and 80% warnings`

**Gumarang, Christian**
- `Add CSV save/load in FileManager`
- `Add Reportable interface and AbstractReport template`
- `Add monthly summary, category and budget reports with export`

**Odover, Sarah Jane**
- `Add menu-driven ConsoleUI and Main entry point`
- `Add JUnit 5 test suite and test scripts`
- `Add UML diagrams, README and project documentation`

### Suggested commit order
Because the classes depend on each other, the project only compiles once all
three members' files are in:

1. **Badilla** – first push (above): build scripts, `.gitignore`, transaction and manager classes.
2. **Gumarang** – `FileManager`, `Fmt`, and the report classes.
3. **Odover** – `ConsoleUI`, `Main`, tests (including `lib/junit-platform-console-standalone-1.10.2.jar`), UML diagrams, README and `docs/`.

After step 3, anyone can check that everything works:
```powershell
.\build.bat
.\test.bat
.\run.bat
```

## Useful commands
| Task | PowerShell command |
|---|---|
| What changed? | `git status` / `git diff` |
| History with authors | `git log --format="%h %an %s"` |
| Undo changes to one file (not yet committed) | `git restore src/expensetracker/ConsoleUI.java` |
| Unstage a file | `git restore --staged <file>` |
| Switch branch | `git checkout main` |
| Delete a merged local branch | `git branch -d feature/<your-task>` |
| Remove the build folder | `Remove-Item -Recurse -Force out` |

The `out/` and `data/` folders are listed in `.gitignore`, so compiled classes and
personal test data are never committed.
