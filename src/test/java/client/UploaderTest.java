package client;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Objects;

public class UploaderTest {

    @Test
    public void amazonSimpleUploadTest() {
        simpleUploadTest(StorageType.AMAZON);
    }

    @Test
    public void localSimpleUploadTest() {
        simpleUploadTest(StorageType.LOCAL);
    }

    private void simpleUploadTest(StorageType type) {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());

        Uploader uploader = new Uploader(type, "bristol-streetview-photos");
        FileHolder upload = uploader.newFileHolder(file);

        CompletionObserver completionObserver = newSynchronizedCompletionObserver();
        upload.setUploadCompletionListener(completionObserver);
        upload.setProgressListener(this::onProgressUpdated);
        upload.setUploadFailureListener(this::onFailure);

        uploader.upload(upload);

        synchronized (completionObserver) {
            try {
                completionObserver.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>> EXITED SYNCHRONISED");

//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void trip2UploadTest() {

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(Objects.requireNonNull(classLoader.getResource("trip2")).getFile());
        Assert.assertTrue("File doesn't exist", folder.exists());

        File[] files = folder.listFiles();

        Uploader uploader = new Uploader(StorageType.LOCAL, "bristol-streetview-photos");
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
                            try {
                                completionObserver.wait(4000); // TODO: 31/07/18 Ask Sion for help
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
            }
        }


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

    private void onFailure(String error) {
        System.out.println("ERROR: " + error);
    }

    @Test
    public void getPhotoTest() {

    }

    @Test
    public void uploadAssertionsTest() throws SQLException, InterruptedException {

        // FIXME: 06/08/18 Still not working properly - likey due to sync (?)

        //        synchronized (this) {
            localSimpleUploadTest();
//            wait(10000);
//        }
        List<String> ids;
        try (DatabaseConnection db = new DatabaseConnection()) {
            ids = db.getPhotosTakenOn(LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 52));
//            ids = db.getPhotosTakenBetween( LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 52),
//                                            LocalDateTime.of(2017, Month.FEBRUARY, 2, 12, 34, 56));
        }


        Objects.requireNonNull(ids, "ids were null");
        Assert.assertEquals("Incorrect array size", 1, ids.size());
        Assert.assertEquals("0236451263344ab88f9940679b1dc59b", ids.get(0));

        System.out.println("HERE");

//        Uploader uploader = new Uploader(StorageType.LOCAL, "bristol-streetview-photos");
//        uploader.deleteAll();

//        try (DatabaseConnection db = new DatabaseConnection()) {
//            ids = db.getPhotosTakenOn(LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 52));
//        } catch (SQLException e) {
//            System.out.println(e.getSQLState());
//        }
//
//        Assert.assertTrue("Incorrect array size", ids.size() == 0);

    }

    // TODO: 06/08/18 test that file gets removed when db rejects it

}