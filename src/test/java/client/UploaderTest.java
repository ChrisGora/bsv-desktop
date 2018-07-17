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
        upload.setCompletionListener(this::onCompleted);
    }

    private void onProgressUpdated(double progress) {
        System.out.println("PROGRESS: " + progress);
    }

    private void onCompleted(UploadHolder uploadHolder) {
        System.out.println("DONE: " + uploadHolder.getKey());
    }

}