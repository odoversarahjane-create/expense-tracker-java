package expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FILE PERSISTENCE: saves / loads transactions and budgets as CSV text files,
 * and exports reports as .txt files.
 */
public class FileManager {
    private static final String TX_HEADER = "type,id,date,category,amount,description";
    private static final String BUDGET_HEADER = "category,monthly_limit";

    private final Path dir;
    private int lastSkipped = 0;

    public FileManager(String directory) {
        this.dir = Paths.get(directory);
    }

    public Path getDirectory() { return dir; }

    /** How many bad lines were ignored during the last load(). */
    public int getLastSkipped() { return lastSkipped; }

    public void save(ExpenseManager manager) throws IOException {
        Files.createDirectories(dir);

        try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("transactions.csv"), StandardCharsets.UTF_8)) {
            w.write(TX_HEADER);
            w.newLine();
            for (Transaction t : manager.getAll()) {
                w.write(t.toCsv());
                w.newLine();
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("budgets.csv"), StandardCharsets.UTF_8)) {
            w.write(BUDGET_HEADER);
            w.newLine();
            for (Map.Entry<String, Double> e : manager.getBudgets().entrySet()) {
                w.write(escapeCsv(e.getKey()) + "," + String.format(java.util.Locale.US, "%.2f", e.getValue()));
                w.newLine();
            }
        }
    }

    /** Loads data into the manager. Returns the number of transactions loaded. */
    public int load(ExpenseManager manager) throws IOException {
        lastSkipped = 0;
        int loaded = 0;

        Path txFile = dir.resolve("transactions.csv");
        if (Files.exists(txFile)) {
            try (BufferedReader r = Files.newBufferedReader(txFile, StandardCharsets.UTF_8)) {
                String line = r.readLine();           // header
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        manager.restore(parseTransaction(line));
                        loaded++;
                    } catch (Exception e) {            // malformed line: skip it, keep going
                        lastSkipped++;
                    }
                }
            }
        }

        Path budgetFile = dir.resolve("budgets.csv");
        if (Files.exists(budgetFile)) {
            try (BufferedReader r = Files.newBufferedReader(budgetFile, StandardCharsets.UTF_8)) {
                String line = r.readLine();           // header
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        List<String> f = parseCsvLine(line);
                        manager.setBudget(f.get(0), Double.parseDouble(f.get(1)));
                    } catch (Exception e) {
                        lastSkipped++;
                    }
                }
            }
        }
        return loaded;
    }

    public Path exportReport(String fileName, String content) throws IOException {
        Path reports = dir.resolve("reports");
        Files.createDirectories(reports);
        Path file = reports.resolve(fileName);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    // ---------------------------------------------------------------------
    private Transaction parseTransaction(String line) throws InvalidTransactionException {
        List<String> f = parseCsvLine(line);
        if (f.size() != 6) {
            throw new InvalidTransactionException("Wrong number of columns.");
        }
        int id = Integer.parseInt(f.get(1).trim());
        LocalDate date = LocalDate.parse(f.get(2).trim());
        String category = f.get(3);
        double amount = Double.parseDouble(f.get(4).trim());
        String description = f.get(5);

        switch (f.get(0).trim()) {
            case "EXPENSE":
                return new Expense(id, description, amount, date, category);
            case "INCOME":
                return new Income(id, description, amount, date, category);
            default:
                throw new InvalidTransactionException("Unknown type: " + f.get(0));
        }
    }

    public static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }
}
