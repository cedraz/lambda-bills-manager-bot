package handler.user;

import handler.enums.ConversationState;
import handler.expense.Expense;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class User {
    public long chat_id;
    public String first_name;
    public String username;
    public ArrayList<Expense> expenses;
    public ConversationState conversationState;
    public Expense inProgressExpense;

    public User(long chat_id, String first_name, String username) {
        this.chat_id = chat_id;
        this.first_name = first_name;
        this.username = username;
        this.conversationState = ConversationState.NONE;
        this.expenses = new ArrayList<>();
    }

    public ConversationState getConversationState() {
        return conversationState;
    }

    public void setConversationState(ConversationState conversationState) {
        this.conversationState = conversationState;
    }

    public Expense getInProgressExpense() {
        return inProgressExpense;
    }

    public void setInProgressExpense(Expense inProgressExpense) {
        this.inProgressExpense = inProgressExpense;
    }

    public void setInProgressExpenseFromMap(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            this.inProgressExpense = null;
            return;
        }

        Expense expense = new Expense(0, "", LocalDate.now());
        if (item.containsKey("amount")) {
            int amount = Integer.parseInt(item.get("amount").n());
            expense.setAmount(amount);
        }
        if (item.containsKey("description")) {
            expense.setDescription(item.get("description").s());
        }
        if (item.containsKey("category")) {
            expense.setCategory(item.get("category").s());
        }
        this.inProgressExpense = expense;
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
