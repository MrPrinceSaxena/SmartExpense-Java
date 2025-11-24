package src;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SmartExpense Swing UI with filters, search, monthly summary and export.
 */
public class SmartExpenseUI extends JFrame {
    private final ExpenseManager manager;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryFilter = new JComboBox<>();
    private final JTextField fromDate = new JTextField(8);
    private final JTextField toDate = new JTextField(8);

    public SmartExpenseUI() {
        manager = new ExpenseManager(new CSVStorage("expenses.csv"));
        setTitle("SmartExpense — Personal Expense Tracker");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[]{"ID","Date","Amount","Category","Note"},0) {
            @Override public boolean isCellEditable(int row, int column){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setAutoCreateRowSorter(true);

        refreshTable();

        JScrollPane scroll = new JScrollPane(table);

        // Top panel with search and filters
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:")); top.add(searchField);
        top.add(new JLabel("Category:")); top.add(categoryFilter);
        top.add(new JLabel("From (dd-MM-yyyy):")); top.add(fromDate);
        top.add(new JLabel("To (dd-MM-yyyy):")); top.add(toDate);
        JButton applyFilter = new JButton("Apply Filters");
        JButton clearFilter = new JButton("Clear Filters");
        top.add(applyFilter); top.add(clearFilter);

        // Buttons panel
        JPanel buttons = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton viewBtn = new JButton("View");
        JButton summaryBtn = new JButton("Monthly Summary");
        JButton exportBtn = new JButton("Export CSV");
        buttons.add(addBtn); buttons.add(editBtn); buttons.add(delBtn);
        buttons.add(viewBtn); buttons.add(summaryBtn); buttons.add(exportBtn);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // Populate category filter
        rebuildCategoryFilter();

        // Listeners
        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a row to edit"); return; }
            String id = model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
            openForm(manager.getById(id));
        });

        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a row to delete"); return; }
            String id = model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
            int conf = JOptionPane.showConfirmDialog(this, "Delete selected expense?","Confirm",JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                manager.remove(id);
                refreshTable();
                rebuildCategoryFilter();
            }
        });

        viewBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a row to view"); return; }
            String id = model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
            Expense ex = manager.getById(id);
            JOptionPane.showMessageDialog(this, ex.toString(), "Expense Details", JOptionPane.INFORMATION_MESSAGE);
        });

        summaryBtn.addActionListener(e -> showSummary());
        exportBtn.addActionListener(e -> exportCSV());

        applyFilter.addActionListener(e -> applyFilters());
        clearFilter.addActionListener(e -> {
            searchField.setText(""); fromDate.setText(""); toDate.setText(""); categoryFilter.setSelectedIndex(0);
            applyFilters();
        });

        // Live search
        searchField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){ applyRowFilter(); }
            public void removeUpdate(DocumentEvent e){ applyRowFilter(); }
            public void changedUpdate(DocumentEvent e){ applyRowFilter(); }
        });

        setVisible(true);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Expense e : manager.getAll()) {
            model.addRow(new Object[]{ e.getId(), e.getDate().format(Expense.FMT), e.getAmount(), e.getCategory(), e.getNote() });
        }
    }

    private void rebuildCategoryFilter() {
        Set<String> cats = new TreeSet<>(manager.getCategories());
        categoryFilter.removeAllItems();
        categoryFilter.addItem("(any)");
        for (String c : cats) categoryFilter.addItem(c);
    }

    private void openForm(Expense exp) {
        JTextField date = new JTextField(exp == null ? "" : exp.getDate().format(Expense.FMT));
        JTextField amt = new JTextField(exp == null ? "" : String.valueOf(exp.getAmount()));
        JTextField cat = new JTextField(exp == null ? "" : exp.getCategory());
        JTextArea note = new JTextArea(exp == null ? "" : exp.getNote(), 4, 20);
        JScrollPane noteScroll = new JScrollPane(note);

        Object[] msg = {
            "Date (dd-MM-yyyy) — leave blank for today:", date,
            "Amount:", amt,
            "Category:", cat,
            "Note:", noteScroll
        };

        int ok = JOptionPane.showConfirmDialog(this, msg, exp == null ? "Add Expense" : "Edit Expense", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                LocalDate d = date.getText().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(date.getText().trim(), Expense.FMT);
                double a = Double.parseDouble(amt.getText().trim());
                String c = cat.getText().trim();
                String n = note.getText().trim();
                if (exp == null) {
                    manager.add(new Expense(d, a, c, n));
                } else {
                    exp.setDate(d); exp.setAmount(a); exp.setCategory(c); exp.setNote(n);
                    manager.update(exp);
                }
                refreshTable();
                rebuildCategoryFilter();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount. Enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use dd-MM-yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving expense: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilters() {
        // First, use manager-side filtering for date range & category then apply search on table
        List<Expense> filtered = manager.getAll();

        String cat = (String) categoryFilter.getSelectedItem();
        if (cat != null && !cat.equals("(any)")) {
            filtered = manager.filterByCategory(cat);
        }

        try {
            if (!fromDate.getText().trim().isEmpty() && !toDate.getText().trim().isEmpty()) {
                LocalDate f = LocalDate.parse(fromDate.getText().trim(), Expense.FMT);
                LocalDate t = LocalDate.parse(toDate.getText().trim(), Expense.FMT);
                filtered = manager.filterByDateRange(f, t);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date(s) in filters. Use dd-MM-yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update table model to show only filtered
        model.setRowCount(0);
        for (Expense e : filtered) {
            model.addRow(new Object[]{ e.getId(), e.getDate().format(Expense.FMT), e.getAmount(), e.getCategory(), e.getNote() });
        }
        applyRowFilter(); // apply text search on top
    }

    private void applyRowFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    private void showSummary() {
        Map<String, Double> sum = manager.monthlySummary();
        if (sum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No expenses recorded.", "Summary", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly Summary (yyyy-MM -> total)\n\n");
        for (Map.Entry<String, Double> e : sum.entrySet()) {
            sb.append(String.format("%s -> %.2f\n", e.getKey(), e.getValue()));
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Monthly Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export current data to CSV");
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File out = fc.getSelectedFile();
                java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(out));
                // write header
                pw.println("id,date,amount,category,note");
                for (Expense e : manager.getAll()) pw.println(e.toCSV());
                pw.close();
                JOptionPane.showMessageDialog(this, "Exported to " + out.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // use system look and feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(SmartExpenseUI::new);
    }
}
