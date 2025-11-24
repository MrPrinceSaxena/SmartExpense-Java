package src;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager handling in-memory list and persistence.
 */
public class ExpenseManager {
    private final List<Expense> expenses = new ArrayList<>();
    private final CSVStorage storage;

    public ExpenseManager(CSVStorage storage) {
        this.storage = storage;
        expenses.addAll(storage.loadAll());
    }

    public List<Expense> getAll() {
        return new ArrayList<>(expenses);
    }

    public void add(Expense e) {
        expenses.add(e);
        persist();
    }

    public boolean remove(String id) {
        boolean removed = expenses.removeIf(e -> e.getId().equals(id));
        if (removed) persist();
        return removed;
    }

    public Expense getById(String id) {
        for (Expense e : expenses) if (e.getId().equals(id)) return e;
        return null;
    }

    public void update(Expense e) {
        // expenses are stored by reference; just persist
        persist();
    }

    public List<Expense> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) return getAll();
        String c = category.trim().toLowerCase();
        return expenses.stream().filter(e -> e.getCategory().toLowerCase().contains(c)).collect(Collectors.toList());
    }

    public List<Expense> filterByDateRange(LocalDate from, LocalDate to) {
        return expenses.stream()
            .filter(e -> !e.getDate().isBefore(from) && !e.getDate().isAfter(to))
            .collect(Collectors.toList());
    }

    public Map<String, Double> monthlySummary() {
        Map<String, Double> map = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Expense e : expenses) {
            String key = e.getDate().format(fmt);
            map.put(key, map.getOrDefault(key, 0.0) + e.getAmount());
        }
        return map;
    }

    public Set<String> getCategories() {
        return expenses.stream().map(Expense::getCategory).filter(s->!s.isEmpty()).collect(Collectors.toSet());
    }

    private void persist() {
        storage.saveAll(expenses);
    }
}
