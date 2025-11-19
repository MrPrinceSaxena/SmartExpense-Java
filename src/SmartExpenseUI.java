package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class SmartExpenseUI extends JFrame {
    private ExpenseManager manager;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public SmartExpenseUI() {
        manager = new ExpenseManager();
        setTitle("SmartExpense – Personal Expense Tracker");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Table Setup
        String[] columns = { "ID", "Category", "Amount", "Date", "Note" };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(tableModel);
        refreshTable();

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        JTextField categoryField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextField noteField = new JTextField();

        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(new JLabel("Note:"));
        inputPanel.add(new JLabel(""));

        inputPanel.add(categoryField);
        inputPanel.add(amountField);
        inputPanel.add(dateField);
        inputPanel.add(noteField);

        JButton addButton = new JButton("Add Expense");
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        // Total Panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel();
        totalPanel.add(totalLabel);
        add(totalPanel, BorderLayout.SOUTH);
        refreshTotal();

        // Action: Add Expense
        addButton.addActionListener(e -> {
            try {
                String category = categoryField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                String note = noteField.getText().trim();
                if (category.isEmpty()) throw new IllegalArgumentException();
                manager.addExpense(category, amount, date, note);
                refreshTable();
                refreshTotal();
                categoryField.setText(""); amountField.setText(""); dateField.setText(LocalDate.now().toString()); noteField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid entry! Please check inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Expense e : manager.listExpenses()) {
            tableModel.addRow(e.toTableRow());
        }
    }

    private void refreshTotal() {
        totalLabel.setText("Total Expense: ₹" + String.format("%.2f", manager.totalAmount()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartExpenseUI().setVisible(true));
    }
}
