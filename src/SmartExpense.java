package src;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class SmartExpense {
    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to SmartExpense — Personal Expense Tracker!");
        while (true) {
            System.out.println("\n1. Add expense");
            System.out.println("2. List expenses");
            System.out.println("3. Show total amount");
            System.out.println("4. Exit");
            System.out.print("Select: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Category: ");
                    String category = scanner.nextLine();
                    System.out.print("Amount: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Date (YYYY-MM-DD): ");
                    LocalDate date = LocalDate.parse(scanner.nextLine());
                    System.out.print("Note: ");
                    String note = scanner.nextLine();
                    manager.addExpense(category, amount, date, note);
                    System.out.println("Expense added!");
                    break;

                case 2:
                    List<Expense> expenses = manager.listExpenses();
                    for (Expense e : expenses) {
                        System.out.println(e);
                    }
                    break;

                case 3:
                    System.out.printf("Total: %.2f\n", manager.totalAmount());
                    break;

                case 4:
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}