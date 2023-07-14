package edu.neu.csye6225.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class MailService {

    static final String SUBJECT = "Please verify your webservice account";
    SGConfig config = new SGConfig();
    public void sendMail(Context context, String email, String token ) throws IOException {
        String HTMLBODY = "<h1>CSYE6225 - webservice</h1>"
                + "<p>DO NOT Reply to this mail</p>"
                + "<p>This email is sent with webservice domain. Please click on the below registration link.</p>";
        String TEXTBODY = "This email is sent with webservice domain. Please click on the below registration link.";

        LambdaLogger logger = context.getLogger();
        String domain = System.getenv("DOMAIN_NAME");
        domain = "demo.domain.me";
        if(domain.equals("")) {
            domain = "demo.domain.me";
            logger.log("Domain env is not set");
        }
        final String FROM = "Sender@"+domain;
        String link = "https://"+domain+"/v1/verifyUserEmail?email="+email+"&token="+token;
        TEXTBODY += " open then the following URL in your browser window: ";
        TEXTBODY += link;
        TEXTBODY += " Thank you! And we are waiting for you";

        HTMLBODY += "<a href='"+link+"'>"
                + "Final step to complete your registration"
                + "</a><br/><br/>"
                + " Thank you! And we are waiting for you";

        logger.log("Verfication link is : " + TEXTBODY);
        Email fromid = new Email(FROM);

        Mail mail = new Mail(fromid, SUBJECT, new Email(email),new Content("text/plain",TEXTBODY));
        SendGrid sg = new SendGrid("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");
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException e){
            logger.log("Unable to send mail to : "+ mail + ". Exception : " + e.getMessage());
            throw e;
        }

    }}
