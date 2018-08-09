package client;

import com.amazonaws.transform.MapEntry;
import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.fail;

public class UploaderTest {

    private String error;

    @Rule
    public final TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        error = null;
    }

    private Uploader newTestUploader() {
        return new Uploader(StorageType.LOCAL, "bristol-streetview-photos");
    }

    @Test
    public void amazonSimpleUploadTest() throws InterruptedException {
        simpleUploadTest(StorageType.AMAZON);
        if (error != null) fail(error);
        System.out.println(name.getMethodName() + ": PASSED");
    }

    @Test
    public void localSimpleUploadTest() throws InterruptedException {
        simpleUploadTest(StorageType.LOCAL);
        if (error != null) fail(error);
        System.out.println(name.getMethodName() + ": PASSED");
    }

    private void simpleUploadTest(StorageType type) throws InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());

        Uploader uploader = newTestUploader();
        uploader.deleteAll();

        FileHolder upload = uploader.newFileHolder(file);

        CompletionObserver completionObserver = newSynchronizedCompletionObserver();
        upload.setDbUpdateCompletionListener(completionObserver);
        upload.setProgressListener(this::onProgressUpdated);
        upload.setUploadFailureListener(this::onFailure);

        uploader.upload(upload);

        synchronized (completionObserver) {
                completionObserver.wait(5000);
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>> EXITED SYNCHRONISED");

//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void trip2UploadTest() throws InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(Objects.requireNonNull(classLoader.getResource("trip2")).getFile());
        Assert.assertTrue("File doesn't exist", folder.exists());

        File[] files = folder.listFiles();

        Uploader uploader = newTestUploader();
        uploader.deleteAll();

        for (File file : Objects.requireNonNull(files, "Files were null")) {
            if (file != null && file.getPath().contains("_E.jpg")) {
                    FileHolder upload = uploader.newFileHolder(file);
                    if (upload != null) {
                        upload.setProgressListener(this::onProgressUpdated);
                        upload.setUploadFailureListener(this::onFailure);

                        CompletionObserver completionObserver = newSynchronizedCompletionObserver();
                        upload.setDbUpdateCompletionListener(completionObserver);

                        uploader.upload(upload);

                        synchronized (completionObserver) {
                            completionObserver.wait(5000); // TODO: 31/07/18 Ask Sion for help
                        }

                    }
            }
        }

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
    public void getPhotoTest() {
        System.out.println(name.getMethodName() + ": PASSED");
    }

    @Test
    public void uploadAssertionsTest() throws SQLException, InterruptedException {

        // FIXME: 06/08/18 Still not working properly - likey due to sync (?)

        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll("bristol-streetview-photos");
        }

//        synchronized (this) {
            simpleUploadTest(StorageType.LOCAL);
//            wait(1000);
//        }

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
    public void removeTest() throws SQLException, InterruptedException {
        uploadAssertionsTest();

        Uploader uploader = newTestUploader();
        uploader.deleteAll();

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

//    public void

}