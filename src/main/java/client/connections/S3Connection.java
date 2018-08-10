package client.connections;

import client.FileHolder;
import client.StorageType;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.util.Objects;

//class S3Connection extends Task<Void> {
public class S3Connection extends StorageConnection {

    private static AmazonS3 s3 = null;
//    private AmazonS3 s3;
    private FileHolder fileHolder;

    public S3Connection(FileHolder fileHolder) {
        super(fileHolder, StorageType.AMAZON);
        if (s3 == null) {

            s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.EU_WEST_2)
                    .build();
        }

        this.fileHolder = fileHolder;
    }

    @Override
    public void copyFile() {
        Objects.requireNonNull(fileHolder.getFile(), "File was null");
        String bucket  = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(fileHolder.getKey(), "Key was null");
        System.out.println("Upload fileHolder method beginning thread: " + Thread.currentThread().getName());
        System.out.println("request created");
            PutObjectRequest request = new PutObjectRequest(fileHolder.getBucket(), fileHolder.getKey(), fileHolder.getFile());
            request.setGeneralProgressListener((progressEvent) -> {

//                ProgressUpdateNotifier notifier = new ProgressUpdateNotifier(fileHolder, progressEvent.getBytesTransferred());
//                Platform.runLater(notifier);

//                System.out.println("PROGRESS: " + progressEvent);

                if (Thread.interrupted()) {
                    s3.shutdown();
                }

                if (progressEvent.getEventType() == ProgressEventType.CLIENT_REQUEST_FAILED_EVENT
                    || progressEvent.getEventType() == ProgressEventType.TRANSFER_FAILED_EVENT
                    || progressEvent.getEventType() == ProgressEventType.TRANSFER_PART_FAILED_EVENT) {

                    fileHolder.onUploadFailure(progressEvent.toString());
                } else {
                    fileHolder.onBytesUploaded(progressEvent.getBytesTransferred());
                }

//                fileHolder.setUploadCompletionListener((fileHolder) -> {
//                    System.out.println("S3 shutdown");
//                    s3.shutdown();
//                });

                // FIXME: 16/07/18 Am I creating a million executors ?? - run with visual VM
            });

            s3.putObject(request);
    }

    // TODO: 24/07/18 Test file removal and wire up with db fail

    @Override
    public void removeFile() {
        String bucket  = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(fileHolder.getKey(), "Key was null");
        DeleteObjectRequest request= new DeleteObjectRequest(bucket, key);
        request.setGeneralProgressListener((progressEvent) -> {
            boolean done = progressEvent.getEventType() == ProgressEventType.TRANSFER_COMPLETED_EVENT;
            if (done) fileHolder.onRemoveSuccess();
            else {

                boolean error = progressEvent.getEventType() == ProgressEventType.CLIENT_REQUEST_FAILED_EVENT
                             || progressEvent.getEventType() == ProgressEventType.TRANSFER_FAILED_EVENT
                             || progressEvent.getEventType() == ProgressEventType.TRANSFER_PART_FAILED_EVENT;
                if (error) fileHolder.onRemoveFailure(progressEvent.toString());
            }
        });
        s3.deleteObject(request);
    }

    @Override
    public void removeAll() {
        throw new RuntimeException("Implement me!!!");
        // TODO: 10/08/18 IMPLEMENT ME!!!!
    }
}
