package handler.user;

import handler.dynamo.DynamoDB;
import handler.enums.ConversationState;
import handler.expense.Expense;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final DynamoDB dynamoDB;

    public UserRepository(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public User findByChatId(long chat_id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(dynamoDB.getTableName())
                .key(Map.of("chat_id", AttributeValue.builder().n(String.valueOf(chat_id)).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDB.getDynamoDbClient().getItem(request).item();

        if (item == null || item.isEmpty()) return null;

        User user = new User(
                chat_id,
                item.get("first_name").s(),
                item.containsKey("username") ? item.get("username").s() : null
        );

        // Define o estado da conversa de forma segura
        if (item.containsKey("conversationState")) {
            String stateAsString = item.get("conversationState").s();
            user.setConversationState(ConversationState.fromString(stateAsString)); // Usando o método seguro
        } else {
            user.setConversationState(ConversationState.NONE);
        }

        return user;
    }

    public Expense getExpenseFromMap(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }

        int amount = item.containsKey("amount") ? Integer.parseInt(item.get("amount").n()) : 0;
        String description = item.containsKey("description") ? item.get("description").s() : "";
        LocalDate date = item.containsKey("date") ? LocalDate.parse(item.get("date").s()) : LocalDate.now();
        String category = item.containsKey("category") ? item.get("category").s() : "Outros";

        return new Expense(amount, description, date, category);
    }

    public String saveUser(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("chat_id", AttributeValue.builder().n(String.valueOf(user.getChat_id())).build());
        item.put("first_name", AttributeValue.builder().s(user.getFirst_name()).build());
        if (user.getUsername() != null) {
            item.put("username", AttributeValue.builder().s(user.getUsername()).build());
        }

        PutItemRequest request = PutItemRequest.builder().tableName(this.dynamoDB.getTableName()).item(item).build();

        try {
            dynamoDB.getDynamoDbClient().putItem(request);
            return "User saved successfully";
        } catch (DynamoDbException e) {
            System.out.println("ERROR saving user to DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to add user", e);
        }
    }

    public String updateUser(User user) {
        Map<String, AttributeValueUpdate> updates = new HashMap<>();

        updates.put("first_name", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(user.getFirst_name()).build())
                .action(AttributeAction.PUT)
                .build());

        if (user.getUsername() != null) {
            updates.put("username", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(user.getUsername()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }

        updates.put("conversationState", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(user.getConversationState().name()).build())
                .action(AttributeAction.PUT)
                .build());

        if (user.getInProgressExpense() != null) {
            updates.put("inProgressExpense", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().m(user.getInProgressExpense().toAttributeValueMap()).build())
                    .action(AttributeAction.PUT)
                    .build());
        } else {
            updates.put("inProgressExpense", AttributeValueUpdate.builder()
                    .action(AttributeAction.DELETE)
                    .build());
        }

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.dynamoDB.getTableName())
                .key(Map.of("chat_id", AttributeValue.builder().n(String.valueOf(user.getChat_id())).build()))
                .attributeUpdates(updates)
                .build();

        try {
            dynamoDB.getDynamoDbClient().updateItem(request);
            return "User updated successfully";
        } catch (DynamoDbException e) {
            System.out.println("ERROR updating user in DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }
}
