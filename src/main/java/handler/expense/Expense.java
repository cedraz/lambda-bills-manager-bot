package handler.expense;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Expense {
    public int amount;
    public String description;
    public LocalDate date;
    public String category;

    public Expense(int amount, String description, LocalDate date) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = "Outros";
    }

    public Expense(int amount, String description, LocalDate date, String category) {
        this.amount = amount * 100; // Store amount in cents
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public Map<String, AttributeValue> toAttributeValueMap() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("amount", AttributeValue.builder().n(String.valueOf(this.amount)).build());
        item.put("description", AttributeValue.builder().s(this.description).build());
        item.put("date", AttributeValue.builder().s(this.date.toString()).build());
        item.put("category", AttributeValue.builder().s(this.category).build());
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public String getAmountAsString() {
        return String.format("%.2f", amount / 100.0);
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "amount=" + amount +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", category='" + category + '\'' +
                '}';
    }
}
