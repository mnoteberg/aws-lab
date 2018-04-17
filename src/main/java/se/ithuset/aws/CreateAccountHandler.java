package se.ithuset.aws;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import se.ithuset.aws.model.Customer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static se.ithuset.aws.model.Constants.QUEUE_NAME;
import static se.ithuset.aws.model.Constants.REGION;
import static se.ithuset.aws.model.Constants.TOPIC_ARN;

public class CreateAccountHandler implements RequestHandler<Customer, String> {

    public String handleRequest(Customer customer, Context context) {
        enrichCustomer(customer);
        publishSqs(customer, context.getLogger());
        publishSns(context.getLogger());
        return "OK";
    }

    private void enrichCustomer(Customer customer) {
        customer.setAccountId(UUID.randomUUID().toString());
        customer.setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        customer.setBalance(0);
    }

    private void publishSqs(Customer customer, LambdaLogger logger) {
        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider()).build();
        GetQueueUrlResult queueUrl = sqs.getQueueUrl(QUEUE_NAME);
        SendMessageRequest smRequest = new SendMessageRequest().withQueueUrl(queueUrl.getQueueUrl())
                .withMessageBody(customer.asJson())
                .withMessageGroupId(UUID.randomUUID().toString())
                .withMessageDeduplicationId(UUID.randomUUID().toString());
        sqs.sendMessage(smRequest);
        logger.log("Message published to sqs: " + smRequest);
    }

    private void publishSns(LambdaLogger logger) {
        AmazonSNS sns = AmazonSNSClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider()).build();
        sns.publish(TOPIC_ARN, "account put on sqs");
        logger.log("Message published to sns");
    }
}