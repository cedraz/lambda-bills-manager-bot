package handler.expense;

import handler.dynamo.DynamoDB;
import handler.expense.Expense;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseRepository {
    private final DynamoDB dynamoDB;

    public ExpenseRepository(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public String saveExpense(long chat_id, Expense expense) {
        Map<String, AttributeValue> expenseMap = expense.toAttributeValueMap();
        String updateExpression = "SET #expenses = list_append(if_not_exists(#expenses, :empty_list), :new_expense)";

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#expenses", "expenses");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":new_expense", AttributeValue.builder()
                .l(AttributeValue.builder().m(expenseMap).build())
                .build());
        expressionAttributeValues.put(":empty_list", AttributeValue.builder().l(List.of()).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.dynamoDB.getTableName())
                .key(Map.of("chat_id", AttributeValue.builder().n(String.valueOf(chat_id)).build()))
                .updateExpression(updateExpression)
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            dynamoDB.getDynamoDbClient().updateItem(request);
            return "Expense added successfully";
        } catch (DynamoDbException e) {
            System.out.println("ERROR adding expense to DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to add expense", e);
        }
    }

    public List<Expense> getExpenses(long chat_id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(dynamoDB.getTableName())
                .key(Map.of("chat_id", AttributeValue.builder().n(String.valueOf(chat_id)).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDB.getDynamoDbClient().getItem(request).item();

        if (item == null || !item.containsKey("expenses")) {
            return List.of();
        }

        List<AttributeValue> expenseAttributes = item.get("expenses").l();
        return expenseAttributes.stream()
                .map(attr -> this.getFromAttributeValueMap(attr.m()))
                .toList();

    }

    public Expense getFromAttributeValueMap(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }

        int amount = item.containsKey("amount") ? Integer.parseInt(item.get("amount").n()) : 0;
        String description = item.containsKey("description") ? item.get("description").s() : "";
        LocalDate date = item.containsKey("date") ? LocalDate.parse(item.get("date").s()) : LocalDate.now();
        String category = item.containsKey("category") ? item.get("category").s() : "Outros";

        return new Expense(amount, description, date, category);
    }
}
