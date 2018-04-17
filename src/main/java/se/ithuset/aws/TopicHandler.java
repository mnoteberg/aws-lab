package se.ithuset.aws;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.ithuset.aws.model.Customer;

import java.io.IOException;

import static se.ithuset.aws.model.Constants.QUEUE_NAME;
import static se.ithuset.aws.model.Constants.REGION;
import static se.ithuset.aws.model.Constants.TABLE_NAME;

public class TopicHandler implements RequestHandler<SNSEvent, Object> {

    public Object handleRequest(SNSEvent request, Context context) {
        context.getLogger().log("Read message from topic " + request.toString());
        Customer customer = readFromSqs(context.getLogger());
        writeToDynamoDb(customer, context.getLogger());
        return "topic";
    }

    private Customer readFromSqs(LambdaLogger logger) {
        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider()).build();
        GetQueueUrlResult queueUrl = sqs.getQueueUrl(QUEUE_NAME);
        ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(queueUrl.getQueueUrl()).withWaitTimeSeconds(15);
        ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(request);
        String body = receiveMessageResult.getMessages().get(0).getBody();
        Customer customer = fromString(body);
        logger.log("Received customer from sqs: " + customer);
        sqs.deleteMessage(queueUrl.getQueueUrl(), receiveMessageResult.getMessages().get(0).getReceiptHandle());
        logger.log("Deleted message from sqs");
        return customer;
    }

    private void writeToDynamoDb(Customer customer, LambdaLogger logger) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider())
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(TABLE_NAME);
        table.putItem(customer.toItem());
        logger.log("Customer written to db: " + customer);
    }

    private Customer fromString(String body) {
        try {
            return new ObjectMapper().readValue(body, Customer.class);
        } catch (IOException e) {
            return null;
        }
    }
}