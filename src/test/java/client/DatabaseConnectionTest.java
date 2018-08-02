package client;

import org.junit.Test;
import org.junit.Assert;


import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public void getPhotoTest() throws SQLException {
        FilePath path = null;

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            connectionTest();
            path = db.getPath("1234567");
        }
        Assert.assertEquals("Wrong bucket", "test-bucket", path.getBucket());
        Assert.assertEquals("Wrong key", "test-key", path.getKey());
    }

    @Test (expected = SQLException.class)
    public void getPhotoNoMatchTest() throws SQLException {
        FilePath path = null;

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            connectionTest();
            path = db.getPath("1234");
        }
        Assert.assertEquals("Wrong bucket", "test-bucket", path.getBucket());
        Assert.assertEquals("Wrong key", "test-key", path.getKey());
    }

    @Test
    public void getPhotoGPS() throws SQLException {

        List<String> ids = new ArrayList<>();

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            connectionTest();
            ids = db.getPhotosWithExactMatch(123.34455, 4555.5600054);
        }

        Assert.assertEquals("List must only have one element",1, ids.size());

        String id = ids.get(0);

        Assert.assertEquals("Wrong id", "1234567", id);

    }

    @Test (expected = SQLException.class)
    public void getPhotoGPSNoMatchTest() throws SQLException {
        List<String> ids = new ArrayList<>();

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll();
            connectionTest();
            ids = db.getPhotosWithExactMatch(123.3445, 4555.5600054);
        }

//        Assert.assertEquals("List must only have one element",1, ids.size());
//
//        String id = ids.get(0);
//
//        Assert.assertEquals("Wrong id", "1234567", id);
    }
}