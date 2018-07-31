package client;

import org.junit.Test;
import org.junit.Assert;


import java.time.LocalDateTime;

public class DatabaseConnectionTest {

    @Test
    public void connectionTest() {
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            int result = db.insertPhotoRow(
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

            Assert.assertEquals("DB result doesn't match", 1, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteTest() {
        connectionTest();

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        connectionTest();
    }

    @Test
    public void getPhotoTest() {
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            connectionTest();
            db.getPhoto("1234567");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}