package se.ithuset.aws.model;

import com.amazonaws.regions.Regions;

public interface Constants {
    String TOPIC_ARN = "arn:aws:sns:us-east-2:123456789012:bankTopic";
    String QUEUE_NAME = "bankQueue.fifo";
    String TABLE_NAME = "bankaccount";
    Regions REGION = Regions.US_EAST_2;
}
