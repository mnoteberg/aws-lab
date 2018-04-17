package se.ithuset.aws;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import se.ithuset.aws.model.Customer;
import se.ithuset.aws.model.GetRequest;

import static se.ithuset.aws.model.Constants.REGION;
import static se.ithuset.aws.model.Constants.TABLE_NAME;

public class GetAccountHandler implements RequestHandler<GetRequest, Customer> {

    public Customer handleRequest(GetRequest request, Context context) {
        Customer customer = readFromDynamoDb(request.getBankAccountId());
        context.getLogger().log("Customer retrieved from dynamo db: " + customer);
        return customer;
    }

    private Customer readFromDynamoDb(String bankAccountId) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider())
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(TABLE_NAME);
        Item item = table.getItem("accountId", bankAccountId);
        return Customer.fromItem(item);
    }
}