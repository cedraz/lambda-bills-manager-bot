package handler.expense;

import handler.dynamo.DynamoDB;
import handler.expense.Expense;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

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
}
