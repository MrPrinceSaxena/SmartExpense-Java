package src;

import java.time.LocalDate;

public class Expense {
    private int id;
    private String category;
    private double amount;
    private LocalDate date;
    private String note;

    public Expense(int id, String category, double amount, LocalDate date, String note) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getNote() { return note; }

    public Object[] toTableRow() {
        return new Object[] { id, category, amount, date, note };
    }
}
