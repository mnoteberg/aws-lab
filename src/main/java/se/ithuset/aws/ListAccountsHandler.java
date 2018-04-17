package se.ithuset.aws;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import se.ithuset.aws.model.Customer;

import java.util.ArrayList;
import java.util.List;

import static se.ithuset.aws.model.Constants.REGION;
import static se.ithuset.aws.model.Constants.TABLE_NAME;

public class ListAccountsHandler implements RequestHandler<Object, List<Customer>> {

    public List<Customer> handleRequest(Object request, Context context) {
        return readFromDynamoDb();
    }

    private List<Customer> readFromDynamoDb() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new ClasspathPropertiesFileCredentialsProvider())
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(TABLE_NAME);
        ItemCollection<ScanOutcome> items = table.scan();
        List<Customer> customers = new ArrayList<>();
        for (Item item : items) {
            customers.add(Customer.fromItem(item));
        }
        return customers;
    }
}