package expensetracker;

/** Program entry point. Optional argument: data folder (default "data"). */
public class Main {
    public static void main(String[] args) {
        String dataDir = args.length > 0 ? args[0] : "data";
        new ConsoleUI(dataDir).run();
    }
}
