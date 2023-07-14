package edu.neu.csye6225.serverless;

import com.sendgrid.SendGrid;

public class SGConfig {

    String appkey="***";

    public SendGrid getSendGrid(){
        return new SendGrid(appkey);
    }


}
