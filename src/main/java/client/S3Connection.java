package client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javafx.application.Platform;
import javafx.concurrent.Task;

// TODO: 13/07/18 Refactor into S3Connection
// Single connection rather than a connection manager. (A non anonymous subclass of Task with a constructor parameter)
// Create a new instance for each file
// Prevents a mess with threading
// Each instance has an associated UploadHolder
// The new class will extend Task
// https://stackoverflow.com/questions/35749456/javafx-supply-arguments-to-task

class S3Connection extends Task<Void> {

    private AmazonS3 s3;
    private UploadHolder upload;

    S3Connection(Regions region, UploadHolder upload) {
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        this.upload = upload;
    }

    @Override
    protected Void call() throws Exception {
        uploadFile();
        return null;
    }

    // Needs to upload a photo and return some sort of reference (a url?)
    private void uploadFile() {
        System.out.println("Upload file method beginning thread: " + Thread.currentThread().getName());
            PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
            request.setGeneralProgressListener((progressEvent) -> {

                ProgressUpdateNotifier notifier = new ProgressUpdateNotifier(upload, progressEvent.getBytesTransferred());
                Platform.runLater(notifier);

                // FIXME: 16/07/18 Am I creating a million executors ?? - run with visual VM
            });

            s3.putObject(request);
    }
}
