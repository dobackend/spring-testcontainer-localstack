package dobackend.com.springtestcontainers;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class AccountEntity {
    private String accountId;
    private String accountEvent;
    private Long sequenceNumber;
    private Long timestamp;
    private String eventType;

    @DynamoDbPartitionKey
    public String getAccountId() {
        return accountId;
    }

    @DynamoDbSortKey
    public Long getTimestamp() {
        return timestamp;
    }

    public String getAccountEvent() {
        return accountEvent;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAccountEvent(String accountEvent) {
        this.accountEvent = accountEvent;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}

