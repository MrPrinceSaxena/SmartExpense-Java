package src;

import java.io.*;
import java.util.*;

/**
 * Simple CSV storage for expenses.
 */
public class CSVStorage {
    private final File file;

    public CSVStorage(String path) {
        this.file = new File(path);
    }

    public List<Expense> loadAll() {
        List<Expense> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Expense e = Expense.fromCSV(line);
                if (e != null) list.add(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public void saveAll(Collection<Expense> expenses) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Expense e : expenses) {
                pw.println(e.toCSV());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
