package handler.user;

import handler.expense.Expense;
import java.util.ArrayList;

public class User {
    public long chat_id;
    public String first_name;
    public String username;
    public ArrayList<Expense> expenses;

    public User(long chat_id, String first_name, String username) {
        this.chat_id = chat_id;
        this.first_name = first_name;
        this.username = username;
        this.expenses = new ArrayList<>();
    }

    public long getChat_id() {
        return chat_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
    }
}
