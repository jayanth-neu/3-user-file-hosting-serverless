package edu.neu.csye6225.serverless;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.util.*;
public class AWSDynamoDbClientService {
    private DynamoDB dynamoDb;
    private AmazonDynamoDB client;
    private static AWSConfig config = new AWSConfig();
    private static String dynamoDbEndpoint = System.getenv("DYNAMO_DB");
    private LambdaLogger logger;
    private String UserTokenMailTable = "UserMail";

    public AWSDynamoDbClientService(Context context) throws Exception {
        logger = context.getLogger();
        if(dynamoDbEndpoint.equals("")) dynamoDbEndpoint="dynamodb.us-east-1.amazonaws.com";
        client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(config.getAwsCredentials())
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint,config.getRegion().getName()))
                .build();
        dynamoDb = new DynamoDB(client);

    }

    public boolean saveMailSent(String email){
        logger.log("marking mail sent : " + email);
        try{
            if(!isTableExist(UserTokenMailTable)) createTable(UserTokenMailTable);
            Table table = dynamoDb.getTable(UserTokenMailTable);
            Item item = new Item()
                    .withPrimaryKey("email", email)
                    .withString("mailSent","true");
            table.putItem(item);
            logger.log("marked mail sent : " + email);
            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            logger.log(e.getMessage());
        }
        return false;
    }

    public boolean isMailSent(String email){
        HashMap<String,AttributeValue> key_to_get =
                new HashMap<String,AttributeValue>();
        key_to_get.put("email", new AttributeValue(email));
        GetItemRequest request = new GetItemRequest()
                .withKey(key_to_get)
                .withTableName(UserTokenMailTable);
        try {
            logger.log("Checking if mail sent : "+ email);
            Map<String,AttributeValue> returned_item =
                    client.getItem(request).getItem();
            if (returned_item != null) {
                Set<String> keys = returned_item.keySet();
                for (String key : keys) {
                    logger.log(key + " : " + returned_item.get(key).toString());
                    System.out.format("%s: %s\n",
                            key, returned_item.get(key).toString());
                    return true;
                }
            } else {
                System.out.format("No item found with the key %s!\n", email);
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        logger.log("Mail not sent for : "+ email);
        return false;
    }

    public void createTable(String name) {
        try {
            logger.log("Creating mail sent tracking table : "+ name);
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName("email")
                    .withAttributeType(ScalarAttributeType.S)); //1

            List<KeySchemaElement> keyDefinitions = new ArrayList<>();
            keyDefinitions.add(new KeySchemaElement()
                    .withAttributeName("email")
                    .withKeyType(KeyType.HASH)); //2

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(name)
                    .withKeySchema(keyDefinitions)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(new ProvisionedThroughput(3L, 3L)); //3

            Table table = dynamoDb.createTable(request); //4
            table.waitForActive();
            logger.log("mail sent tracking table creation completed : "+ name);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public boolean isTableExist(String tableName) {
        try {
            logger.log("Check if table already exists : "+ tableName);
            TableDescription tableDescription = dynamoDb.getTable(tableName).describe();
            System.out.println("Table description: " + tableDescription.getTableStatus());

            return true;
        } catch (com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException rnfe) {
            System.out.println("Table does not exist");
        }
        return false;

    }

}
