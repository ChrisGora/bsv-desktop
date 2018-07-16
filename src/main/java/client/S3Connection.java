package client;

import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javafx.application.Platform;
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

class S3Connection extends Task<UploadHolder>{

    private AmazonS3 s3;
    private UploadHolder upload;

    S3Connection(Regions region, UploadHolder upload) {
//        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain();
//        awsCredentialsProvider.getCredentials();
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        this.upload = upload;

    }

//    public void listBuckets() {
//        for (Bucket bucket : s3.listBuckets()) {
//            System.out.println("Found bucket: " + bucket.getName());
//        }
//    }

    @Override
    protected UploadHolder call() throws Exception {
        uploadFile();
//        updateProgress();
        return upload;
    }

    // Needs to upload a photo and return some sort of reference (a url?)
    public void uploadFile() {
        System.out.println("Upload file method beginning thread: " + Thread.currentThread().getName());
            PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
            request.setGeneralProgressListener((progressEvent) -> {

                ProgressUpdateNotifier notifier = new ProgressUpdateNotifier(upload, progressEvent.getBytesTransferred());
                Platform.runLater(notifier);

//                System.out.println("Progresslistener thread: " + Thread.currentThread().getName());
//                ProgressEventType type = progressEvent.getEventType();
//                upload.onBytesUploaded(progressEvent.getBytesTransferred());
//                if (type == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
//                    System.out.println("TRANSFER COMPLETED");
//                }
            });

//            request.setGeneralProgressListener((progressEvent) -> {
//                onUpdate(progressEvent.getBytesTransferred());
//            });

            s3.putObject(request);
        System.out.println("Upload file method end thread: " + Thread.currentThread().getName());

//        });
//        System.out.println(result);
//        return "";
    }

//    private void onUpdate(long bytes) {
//        ProgressUpdateNotifier notifier = new ProgressUpdateNotifier(upload, bytes);
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.submit(notifier);
//    }

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
