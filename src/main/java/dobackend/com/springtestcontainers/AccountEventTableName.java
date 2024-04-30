package dobackend.com.springtestcontainers;

import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;

public class AccountEventTableName implements DynamoDbTableNameResolver {
    @Override
    public <T> String resolve(Class<T> clazz) {
        return "account_events";
    }
}
