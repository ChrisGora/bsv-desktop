package client;

import org.junit.Assert;
import org.junit.Test;


import java.sql.Timestamp;

import static org.junit.Assert.*;

public class RdsConnectionTest {

    @Test
    public void authTokenTest() {
        try (RdsConnection rds = new RdsConnection()) {
            rds.insertPhotoRow(
                    "123456",
                    1000,
                    1000,
                    new Timestamp(new java.util.Date().getTime()),
                    new Timestamp(new java.util.Date().getTime()),
                    123.34455,
                    4555.5600054,
                    "12345567",
                    12,
                    "test-bucket",
                    "test-key"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}