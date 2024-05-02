package dobackend.com.springtestcontainers.user.event;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.UUID;

@DynamoDbBean
public class UserEntity {
    private UUID userId;
    private String sortKey;
    private String userEvent;
    private Long sequenceNumber;
    private String eventType;
    private Instant timeStamp;

    public static final String SORT_PREFIX = "U#";

    @DynamoDbPartitionKey
    @DynamoDbConvertedBy(UUIDtoString.class)
    public UUID getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    public String getSortKey() {
        return sortKey;
    }

    public String getUserEvent() {
        return userEvent;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public void setUserEvent(String userEvent) {
        this.userEvent = userEvent;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }
}

