#!/bin/bash

table_name="user_events"
hash_key="userId"
range_key="sortkey"

# Thanks to https://github.com/mattwcole/local-dynamodb-stream/blob/master/provision.sh for this setup.


echo "Creating lambda function"

awslocal lambda create-function \
--function-name user-events-function \
--runtime java21 \
--handler net.roodt.Handler::handleRequest \
--timeout 30 \
--zip-file fileb:///tmp/lambda-0.0.1-SNAPSHOT-all.jar \
--role arn:aws:iam::000000000000:role/user-events-function-role

echo "Creating dynamodb table"

streamArn=$(awslocal dynamodb create-table \
    --table-name user_events \
    --attribute-definitions \
        AttributeName=userId,AttributeType=S \
        AttributeName=sortKey,AttributeType=S \
    --key-schema \
        AttributeName=userId,KeyType=HASH \
        AttributeName=sortKey,KeyType=RANGE \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_IMAGE \
    --billing-mode PAY_PER_REQUEST \
    --query 'TableDescription.LatestStreamArn' \
    --output text)

awslocal lambda create-event-source-mapping \
    --function-name user-events-function \
    --event-source $streamArn \
    --batch-size 1 \
    --starting-position TRIM_HORIZON

echo "DynamoDB table '$table_name' created successfully with hash key '$hash_key' and range key '$range_key'"
echo "Executed init-dynamodb.sh"

echo "Creating SNS Topic"

awslocal sns create-topic \
    --name user-events-topic

awslocal sns set-topic-attributes \
    --topic-arn arn:aws:sns:us-east-1:000000000000:user-events-topic \
    --attribute-name DisplayName \
    --attribute-value UserEventsTopic

awslocal sqs create-queue \
    --queue-name user-events-queue
