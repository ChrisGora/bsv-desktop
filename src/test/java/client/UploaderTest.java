package client;

import org.junit.Test;

import java.io.File;
import java.util.Objects;

public class UploaderTest {

    @Test
    public void uploadTest() {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());
        Uploader uploader = new Uploader();
        UploadHolder upload = uploader.upload(file);
        upload.setProgressListener(this::onProgressUpdated);

        CompletionObserver completionObserver = new CompletionObserver() {
            @Override
            public void onDone(UploadHolder uploadHolder) {
                synchronized (this) {
                    onCompleted(uploadHolder);
                    notifyAll();
                }
            }
        };

        upload.setCompletionListener(this::onCompleted);
        upload.setFailureListener(this::onFailure);

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

    private void onProgressUpdated(double progress) {
        System.out.println("PROGRESS: " + progress);
    }

    private void onCompleted(UploadHolder uploadHolder) {
//        synchronized ()
        System.out.println("DONE: " + uploadHolder.getKey());
    }

    private void onFailure(String error) {
        System.out.println("ERROR: " + error);
    }
}