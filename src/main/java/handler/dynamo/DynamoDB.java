package handler.dynamo;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDB {
    public static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    private static final String TABLE_NAME = "BillsBotUsers";

    public String getTableName() {
        return TABLE_NAME;
    }

    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }
}
