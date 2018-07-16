package client;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;

class RdsConnection {

    private static final Region REGION = Region.getRegion(Regions.EU_WEST_2);
    private static final String HOSTNAME = "rds-mysql-uob-bristolstreetview.crvuxxvm3uvv.eu-west-2.rds.amazonaws.com";
    private static final int PORT = 3306;
    private static final String USERNAME = "bsv-db-user";

    private String authToken;

    public RdsConnection() {
        this.authToken = generateAuthToken();
        System.out.println(authToken);
    }

    private String generateAuthToken() {
        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator
                .builder()
                .credentials(new DefaultAWSCredentialsProviderChain())
                .region(REGION)
                .build();
        String authToken = generator.getAuthToken(
                GetIamAuthTokenRequest
                        .builder()
                        .hostname(HOSTNAME)
                        .port(PORT)
                        .userName(USERNAME)
                        .build()
        );

        return authToken;
    }
}
