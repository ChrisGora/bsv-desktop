package client;

import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

//class S3Connection extends Task<Void> {
class S3Connection implements Runnable {

    private static AmazonS3 s3 = null;
//    private AmazonS3 s3;
    private UploadHolder upload;

    // TODO: 20/07/18 replace region with s3

    S3Connection(Regions region, UploadHolder upload) {
        if (s3 == null) {

            s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(region)
                    .build();
        }

        this.upload = upload;
    }

//    @Override
//    protected Void call() throws Exception {
//        uploadFile();
//        return null;
//    }

    @Override
    public void run() {
        uploadFile();
    }

    // Needs to upload a photo and return some sort of reference (a url?)
    private void uploadFile() {
        System.out.println("Upload file method beginning thread: " + Thread.currentThread().getName());
        System.out.println("request created");
            PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
            request.setGeneralProgressListener((progressEvent) -> {

//                ProgressUpdateNotifier notifier = new ProgressUpdateNotifier(upload, progressEvent.getBytesTransferred());
//                Platform.runLater(notifier);

//                System.out.println("PROGRESS: " + progressEvent);

                if (Thread.interrupted()) {
                    s3.shutdown();
                }

                if (progressEvent.getEventType() == ProgressEventType.CLIENT_REQUEST_FAILED_EVENT
                    || progressEvent.getEventType() == ProgressEventType.TRANSFER_FAILED_EVENT
                    || progressEvent.getEventType() == ProgressEventType.TRANSFER_PART_FAILED_EVENT) {

                    upload.onUploadFailure(progressEvent.toString());
                } else {
                    upload.onBytesUploaded(progressEvent.getBytesTransferred());
                }

//                upload.setUploadCompletionListener((upload) -> {
//                    System.out.println("S3 shutdown");
//                    s3.shutdown();
//                });

                // FIXME: 16/07/18 Am I creating a million executors ?? - run with visual VM
            });



            s3.putObject(request);
    }

    private void removeFile() {
        DeleteObjectRequest request= new DeleteObjectRequest(upload.getBucket(), upload.getKey());
//        request.setGeneralProgressListener();
        s3.deleteObject(request);
    }
}
