package client;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RdsConnectionTest {

    @Test
    public void authTokenTest() {
        RdsConnection rds = new RdsConnection();
        assertNotNull("Auth token: ", rds.getAuthToken());
    }
}