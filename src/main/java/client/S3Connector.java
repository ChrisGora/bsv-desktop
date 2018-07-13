package client;

import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: 13/07/18 Refactor into S3Connection
// Single connection rather than a connection manager. (A non anonymous subclass of Task with a constructor parameter)
// Create a new instance for each file
// Prevents a mess with threading
// Each instance has an associated UploadHolder
// The new class will extend Task
// https://stackoverflow.com/questions/35749456/javafx-supply-arguments-to-task

class S3Connector extends Service<UploadHolder>{

    private AmazonS3 s3;
    private ExecutorService executor;

    S3Connector(Regions region) {
//        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain();
//        awsCredentialsProvider.getCredentials();
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        executor = Executors.newFixedThreadPool(4);
    }

    public void listBuckets() {
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println("Found bucket: " + bucket.getName());
        }
    }

    @Override
    protected Task<UploadHolder> createTask() {
        return null;
    }

    // Needs to upload a photo and return some sort of reference (a url?)
    public void uploadFile(UploadHolder upload) {
        executor.submit(() -> {
            PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
            request.setGeneralProgressListener((progressEvent) -> {
                ProgressEventType type = progressEvent.getEventType();
                upload.onBytesUploaded(progressEvent.getBytesTransferred());
                if (type == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
                    System.out.println("TRANSFER COMPLETED");
                }
            });
            s3.putObject(request);
        });
//        System.out.println(result);
//        return "";
    }

    /*public Task createFileUploadTast(UploadHolder uploadHolder) {
        return new Task(uploadHolder) {
//            private final UploadHolder upload = uploadHolder;
            @Override
            protected Object call() throws Exception {
                PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
                request.setGeneralProgressListener((progressEvent) -> {
                    ProgressEventType type = progressEvent.getEventType();
                    upload.onBytesUploaded(progressEvent.getBytesTransferred());
                    if (type == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
                        System.out.println("TRANSFER COMPLETED");
                    }
                });
                s3.putObject(request);
                return true;
            }
        };
    }
*/
}
