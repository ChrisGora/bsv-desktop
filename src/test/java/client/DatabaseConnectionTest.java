package client;

import org.junit.Test;


import java.time.LocalDateTime;

public class DatabaseConnectionTest {

    @Test
    public void connectionTest() {
        try (DatabaseConnection rds = new DatabaseConnection()) {
            rds.insertPhotoRow(
                    "1234567",
                    1000,
                    1000,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
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