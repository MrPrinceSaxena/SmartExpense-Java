package src;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ExpenseManager {
    private List<Expense> expenses;
    private int nextId;
    private final String FILE = "expenses.csv";

    public ExpenseManager() {
        expenses = new ArrayList<>();
        nextId = 1;
        loadExpenses();
    }

    public void addExpense(String category, double amount, LocalDate date, String note) {
        Expense expense = new Expense(nextId++, category, amount, date, note);
        expenses.add(expense);
        saveExpenses();
    }

    public List<Expense> listExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public double totalAmount() {
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE))) {
            for (Expense e : expenses) {
                writer.write(e.getId() + "," + e.getCategory() + "," +
                        e.getAmount() + "," + e.getDate() + "," + e.getNote());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses.");
        }
    }

    private void loadExpenses() {
        File file = new File(FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length == 5) {
                    int id = Integer.parseInt(data[0]);
                    String category = data[1];
                    double amount = Double.parseDouble(data[2]);
                    LocalDate date = LocalDate.parse(data[3]);
                    String note = data[4];
                    expenses.add(new Expense(id, category, amount, date, note));
                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading expenses.");
        }
    }
}