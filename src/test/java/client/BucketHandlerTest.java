package client;

import client.databaseConnections.DatabaseConnection;
import client.databaseConnections.ImageMetadata;
import client.handler.ConcreteBucketHandler;
import client.storageConnections.StorageType;
import client.handler.BucketHandler;
import client.handler.FileHolder;
import client.observers.CompletionObserver;
import client.util.Log;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BucketHandlerTest {

    private String error;

    @Rule
    public final TestName name = new TestName();

    @Before
    public void setUp() throws IOException {
        error = null;
        deleteAll();
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        Log.setDebugging();
        Log.setVerbose();
        deleteAll();
    }

    @AfterClass
    public static void tearDownClass() {
//        deleteAll();
    }

    private static void deleteAll() throws IOException {
        BucketHandler localBucketHandler = newTestUploader(StorageType.LOCAL);
        localBucketHandler.deleteAll(null);
        localBucketHandler.close();
    }

    private static BucketHandler newTestUploader() {
        return newTestUploader(StorageType.LOCAL);
    }

    private static BucketHandler newTestUploader(StorageType type) {
        return new ConcreteBucketHandler("bristol-streetview-photos", type);
    }

//    @Test
//    public void amazonSimpleUploadTest() throws InterruptedException {
//        simpleUploadTest(StorageType.AMAZON);
//        if (error != null) fail(error);
//        System.out.println(name.getMethodName() + ": PASSED");
//    }

    @Test
    public void localSimpleUploadTest() throws InterruptedException, IOException {
        simpleUploadTest(StorageType.LOCAL);
        if (error != null) fail(error);
        System.out.println(name.getMethodName() + ": PASSED");
    }

    private void simpleUploadTest(StorageType type) throws InterruptedException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());

        try (BucketHandler bucketHandler = newTestUploader(type)) {

            bucketHandler.deleteAll(null);

            FileHolder upload = bucketHandler.newFileHolder(file);

            CompletionObserver completionObserver = newSynchronizedCompletionObserver();
            upload.setDbUpdateCompletionListener(completionObserver);
            upload.setProgressListener(this::onProgressUpdated);
            upload.setUploadFailureListener(this::onFailure);

            bucketHandler.upload(upload);

            synchronized (completionObserver) {
               completionObserver.wait(5000);
            }

            System.out.println(">>>>>>>>>>>>>>>>>>>>>> EXITED SYNCHRONISED");
        }

//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void trip2UploadTest() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(Objects.requireNonNull(classLoader.getResource("trip2")).getFile());
        Assert.assertTrue("File doesn't exist", folder.exists());

        File[] files = folder.listFiles();
        assertEquals("Incorrect number of files read in", 79 * 2, files.length);

        BucketHandler bucketHandler = newTestUploader();
//        bucketHandler.deleteAll(null);

        for (File file : Objects.requireNonNull(files, "Files were null")) {
            if (file != null && file.getPath().contains("_E.jpg")) {
                    FileHolder upload = bucketHandler.newFileHolder(file);
                    if (upload != null) {
                        upload.setProgressListener(this::onProgressUpdated);
                        upload.setUploadFailureListener(this::onFailure);

                        CompletionObserver completionObserver = newSynchronizedCompletionObserver();
                        upload.setDbUpdateCompletionListener(completionObserver);
                        upload.setDbFailureListener(this::onFailure);

                        bucketHandler.upload(upload);

                        synchronized (completionObserver) {
                            completionObserver.wait(15000);
                        }

                    }
            }
        }

        // Maybe this is the issue?
        bucketHandler.saveJustUploadedAsNewRoute(null, 1);
        bucketHandler.close();

        if (error != null) fail(error);
        System.out.println(name.getMethodName() + ": PASSED");
    }

    private CompletionObserver newSynchronizedCompletionObserver() {
        return new CompletionObserver() {
            @Override
            public void onDone(FileHolder uploadHolder) {
                synchronized (this) {
                    System.out.println("Synchronised OnDone");
                    onCompleted(uploadHolder);
                    notify();
                    notifyAll();
                }
            }
        };
    }

    private void onProgressUpdated(double progress) {
//        System.out.println("PROGRESS: " + progress);
    }

    private void onCompleted(FileHolder fileHolder) {
//        synchronized ()
        System.out.println("DONE: " + fileHolder.getKey());
    }

    private void onFailure(String error) throws AssertionError {
        this.error = error;
        System.out.println("ERROR: " + error);
    }

    @Test
    public void uploadAssertionsTest() throws SQLException, InterruptedException, IOException {

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll("bristol-streetview-photos");
        }

        simpleUploadTest(StorageType.LOCAL);
        if (error != null) fail(error);

        List<ImageMetadata> images;

        try (DatabaseConnection db = new DatabaseConnection()) {
            images = db.getPhotosTakenOn(LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 52));
        }

        Objects.requireNonNull(images, "images were null");
        Assert.assertEquals("Incorrect array size", 1, images.size());
        Assert.assertEquals("0236451263344ab88f9940679b1dc59b", images.get(0).getId());

        System.out.println(name.getMethodName() + ": PASSED");
    }

    @Test
    public void removeTest() throws SQLException, InterruptedException, IOException {
        uploadAssertionsTest();

        BucketHandler bucketHandler = newTestUploader();
        bucketHandler.deleteAll(null);

        String exception = null;
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.getPhotosTakenOn(LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 52));
        } catch (SQLException e) {
            exception = e.getMessage();
        }

        Assert.assertNotNull("No exception was thrown", exception);
        Assert.assertEquals("Incorrect exception","ResultSet was empty", exception);

        System.out.println(name.getMethodName() + ": PASSED");

    }

    // TODO: 06/08/18 test that file gets removed when db rejects it


    @Test
    public void getPhotoTest() throws InterruptedException, IOException {

        try (BucketHandler bucketHandler = newTestUploader()) {
            bucketHandler.deleteAll(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            trip2UploadTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (error != null) fail(error);

        try (DatabaseConnection db = new DatabaseConnection()) {
            List<ImageMetadata> images = db.getPhotosAround(51, 5, -2, 5);
            assertEquals("Incorrect number of images in the database", 79, images.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        BucketHandler bucketHandler = newTestUploader();
        PhotoSet set = bucketHandler.getPhotosAround(51.45868, -2.60385, 100);
        bucketHandler.close();
        Objects.requireNonNull(set, "PhotoSet was null");

        System.out.println(set.getIds());
        Assert.assertEquals("Incorrect number of elements in the set", 79, set.getIds().size());
        Assert.assertEquals("Incorrect number of elements in the set", 79, set.getDistances().size());
        Assert.assertEquals("Incorrect number of elements in the set", 79, set.getImages().size());

        List<String> ids = set.getIds();

        String id = ids.get(0);
        testImage(
                set,
                id,
                "b96810a1aaa843c09b1b8315e588a4e6",
                51.45794530000263,
                -2.6036475,
                82.89,
                0.1
                );

        id = ids.get(ids.size() - 1);
        testImage(
                set,
                id,
                "546c16c46804439fa2b46164aae8d3c5",
                51.4552715,
                -2.603030303030303,
                383.24,
                0.5
        );

        System.out.println(name.getMethodName() + ": PASSED");
    }

    @Test
    public void saveImagesTest() throws Exception {
        trip2UploadTest();
        try (BucketHandler bucketHandler = newTestUploader()) {
            PhotoSet set = bucketHandler.getPhotosAround(51.45868, -2.60385, 100);
            bucketHandler.downloadPhotoSet(null, set);
        }
    }

    private void testImage(
            PhotoSet set,
            String id,
            String expectedId,
            double expectedLatitude,
            double expectedLongitude,
            double expectedDistance,
            double delta
    ) {
        double latitude = set.getImages().get(id).getLatitude();
        double longitude = set.getImages().get(id).getLongitude();
        double distance = set.getDistances().get(id);

        Assert.assertEquals("Wrong id", expectedId, id);
        Assert.assertEquals("Wrong latitude", expectedLatitude, latitude, 0);
        Assert.assertEquals("Wrong longitude", expectedLongitude, longitude, 0);
        Assert.assertEquals("Wrong distance", expectedDistance, distance, delta);
    }

}