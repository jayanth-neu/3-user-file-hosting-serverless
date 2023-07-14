package edu.neu.csye6225.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class SNSLogger implements RequestHandler<SNSEvent, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //private static AWSSesClientService sesClientService;
    private static MailService sgClientService;
    private static AWSDynamoDbClientService dynamoDbClientService;

//    public static AWSSesClientService getSesClientService() {
//        sesClientService = new AWSSesClientService();
//        return sesClientService;
//    }
    public static MailService getSesClientService() {
        sgClientService = new MailService();
        return sgClientService;
    }

    //static AWSDynamoDbClientService dynamoDbClientService = new AWSDynamoDbClientService();
    @java.lang.Override
    public String handleRequest(SNSEvent snsEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Invocation started at : " + new Date());
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        logger.log("EVENT: " + gson.toJson(snsEvent));
        if(snsEvent.getRecords().size() == 0 || snsEvent.getRecords() == null){
            logger.log("Empty SNS request");
            return "";
        }
        String message = snsEvent.getRecords().get(0).getSNS().getMessage();
        logger.log("message is : " + message);
        try{
            //dynamoDbClientService = new AWSDynamoDbClientService(context);
            int loc = message.indexOf(":");
            String mail = message.substring(0, loc);
            String token = message.substring(loc+1);
//            if(dynamoDbClientService.isMailSent(mail)) {
//                logger.log("Mail already sent for : "+mail);
//                return "Mail sent already";
//            }
            //else{
                getSesClientService().sendMail(context, mail, token);
                //dynamoDbClientService.saveMailSent(mail);
                logger.log("Mail sent successfully for : "+mail);
            //}
        } catch (Exception ex){
            logger.log("Email was not sent. Error message: " + ex.getMessage());
        }

        logger.log("Invocation ended at : " + new Date());
        return message;
    }
}
