#!/bin/bash

table_name="account_events"
hash_key="accountId"
range_key="timestamp"

awslocal dynamodb create-table \
    --table-name account_events \
    --attribute-definitions \
        AttributeName=accountId,AttributeType=S \
        AttributeName=timestamp,AttributeType=N \
    --key-schema \
        AttributeName=accountId,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST

echo "DynamoDB table '$table_name' created successfully with hash key '$hash_key' and range key '$range_key'"
echo "Executed init-dynamodb.sh"