package edu.neu.csye6225.serverless;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;

import java.util.ArrayList;
import java.util.List;

public class AWSConfig {

    private static String region = System.getenv("AWS_REGION");

    public AWSCredentialsProvider getAwsCredentials() throws Exception {
            List<AWSCredentialsProvider> providers = new ArrayList<>();
            providers.add(InstanceProfileCredentialsProvider.getInstance());
            providers.add(new DefaultAWSCredentialsProviderChain());
            AWSCredentialsProvider cp = new AWSCredentialsProviderChain(
                    providers.toArray(new AWSCredentialsProvider[providers.size()]));
            try{
                cp.getCredentials();
            } catch (SdkClientException e){
                throw new Exception("Unable to load AWS credentials from any provider");
            }
            return cp;
    }

    public Regions getRegion() {
        if(region.equals("")) region= "us-east-1";
        return Regions.fromName(region);
    }
}
