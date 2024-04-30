package dobackend.com.springtestcontainers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public record AccountEvent(
        EventType eventType,
        Account account,
        long sequenceNumber
) {

    public AccountEntity mapToDynamoEntity(ObjectMapper objectMapper) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setAccountId(this.account.accountId().toString());
        accountEntity.setTimestamp(Instant.now().toEpochMilli());
        accountEntity.setAccountEvent(toJsonString(objectMapper));
        accountEntity.setEventType(this.eventType.name());
        return accountEntity;
    }

    private String toJsonString(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
