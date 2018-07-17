package client;

import org.junit.Assert;
import org.junit.Test;



import static org.junit.Assert.*;

public class RdsConnectionTest {

    @Test
    public void authTokenTest() {
        RdsConnection rds = new RdsConnection();
//        assertNotNull("Auth token: ", rds.getAuthToken());
        rds.insertPhotoRow(
                "123456",
                1000,
                1000,
                new java.sql.Date(new java.util.Date().getTime()),
                123.34455,
                4555.5600054,
                "12345567",
                12
        );

        rds.closeConnection();
    }
}