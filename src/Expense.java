package src;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Expense model with dd-MM-yyyy date format.
 */
public class Expense {
    private String id;
    private LocalDate date;
    private double amount;
    private String category;
    private String note;
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public Expense(LocalDate date, double amount, String category, String note) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.amount = amount;
        this.category = category == null ? "" : category;
        this.note = note == null ? "" : note;
    }

    public Expense(String id, LocalDate date, double amount, String category, String note) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category == null ? "" : category;
        this.note = note == null ? "" : note;
    }

    public String getId(){ return id; }
    public LocalDate getDate(){ return date; }
    public double getAmount(){ return amount; }
    public String getCategory(){ return category; }
    public String getNote(){ return note; }

    public void setDate(LocalDate d){ this.date = d; }
    public void setAmount(double a){ this.amount = a; }
    public void setCategory(String c){ this.category = c; }
    public void setNote(String n){ this.note = n; }

    // Simple CSV escaping (replace commas in text with semicolon)
    public String toCSV() {
        return String.join(",",
            id,
            date.format(FMT),
            String.valueOf(amount),
            category.replace(",", ";"),
            note.replace(",", ";").replace("\\n"," ")
        );
    }

    public static Expense fromCSV(String line) {
        String[] p = line.split(",", 5);
        if (p.length < 4) return null;
        LocalDate d = LocalDate.parse(p[1], FMT);
        double amt = Double.parseDouble(p[2]);
        String cat = p[3];
        String note = p.length > 4 ? p[4] : "";
        return new Expense(p[0], d, amt, cat, note);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %.2f | %s | %s", id, date.format(FMT), amount, category, note);
    }
}
