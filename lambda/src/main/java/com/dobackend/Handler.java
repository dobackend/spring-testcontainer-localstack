package com.dobackend;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.net.URI;
import java.util.Map;

public class Handler implements RequestHandler<DynamodbEvent, Void> {

    @Override
    public Void handleRequest(DynamodbEvent input, Context context) {

        String topicArn ="arn:aws:sns:us-east-1:000000000000:user-events-topic";

        LambdaLogger logger = context.getLogger();

        try (SnsClient snsClient = SnsClient.builder()
            .region(Region.US_EAST_1)
                .endpointOverride(URI.create("http://sns.localhost.localstack.cloud:4566"))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")))
            .build()) {

            SubscribeResponse subscribeResponse = snsClient.subscribe(SubscribeRequest.builder()
                    .attributes(Map.of("RawMessageDelivery", "true"))
                    .protocol("sqs")
                    .topicArn(topicArn)
                    .endpoint("arn:aws:sqs:us-east-1:000000000000:user-events-queue")
                    .build());

            logger.log("Subscription ARN: " + subscribeResponse.subscriptionArn());

            input.getRecords().forEach(r -> {
                logger.log("HELLO FROM THE LAMBDA");
                logger.log(r.getEventID());
                logger.log(r.getEventName());

                r.getDynamodb().getNewImage().forEach((k, v) -> {
                    logger.log("KEY: " + k + " VALUE: " + v);
                });

                logger.log("\n##################################################################\n");
                PublishRequest request = PublishRequest.builder()
                        .message(r.getDynamodb().getNewImage().get("userEvent").getS())
                        .topicArn(topicArn)
                        .build();
                PublishResponse result = snsClient.publish(request);
                logger.log(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());
                logger.log("\n##################################################################\n");

                logger.log("ALL DONE HERE");
            });

        } catch (SnsException e) {
            logger.log("snsException: " + e);
        } catch (Exception e) {
            logger.log("Exception: " + e);
        }


        logger.log("THERE ARE:" + input.getRecords().size() +  " records" );




        return null;
    }
}
