package client;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

public class UploaderTest {

    @Test
    public void simpleUploadTest() {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());

        Uploader uploader = new Uploader(StorageType.AMAZON);
        FileHolder upload = uploader.newUploadHolder(file);
        upload.setProgressListener(this::onProgressUpdated);

        CompletionObserver completionObserver = new CompletionObserver() {
            @Override
            public void onDone(FileHolder uploadHolder) {
                synchronized (this) {
                    onCompleted(uploadHolder);
                    notifyAll();
                }
            }
        };

        upload.setUploadCompletionListener(this::onCompleted);
        upload.setUploadFailureListener(this::onFailure);

        uploader.upload(upload);

        synchronized (completionObserver) {
            try {
                completionObserver.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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

        Uploader uploader = new Uploader(StorageType.LOCAL);
        uploader.deleteAll();

        for (File file : Objects.requireNonNull(files, "Files were null")) {
            if (file != null && file.getPath().contains("_E.jpg")) {
                    FileHolder upload = uploader.newUploadHolder(file);
                    if (upload != null) {
                        upload.setProgressListener(this::onProgressUpdated);
//                        upload.setUploadCompletionListener(this::onCompleted);
                        upload.setUploadFailureListener(this::onFailure);

                        CompletionObserver completionObserver = new CompletionObserver() {
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

                        upload.setUploadCompletionListener(completionObserver);

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
}