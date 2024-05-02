#!/bin/bash

table_name="account_events"
hash_key="accountId"
range_key="timestamp"

awslocal dynamodb create-table \
    --table-name user_events \
    --attribute-definitions \
        AttributeName=userId,AttributeType=S \
        AttributeName=sortKey,AttributeType=S \
    --key-schema \
        AttributeName=userId,KeyType=HASH \
        AttributeName=sortKey,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST

echo "DynamoDB table '$table_name' created successfully with hash key '$hash_key' and range key '$range_key'"
echo "Executed init-dynamodb.sh"