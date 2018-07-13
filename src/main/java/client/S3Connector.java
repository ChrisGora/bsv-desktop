package client;

import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class S3Connector {

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

    // Needs to upload a photo and return some sort of reference (a url?)
    public void uploadFile(UploadHolder upload) {
        executor.submit(() -> {
            PutObjectRequest request = new PutObjectRequest(upload.getBucket(), upload.getKey(), upload.getFile());
            request.setGeneralProgressListener((progressEvent) -> {
                ProgressEventType type = progressEvent.getEventType();
                upload.setMostRecentProgressEvent(progressEvent);
                if (type == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
                    System.out.println("TRANSFER COMPLETED");
                }
            });
            s3.putObject(request);
        });
//        System.out.println(result);
//        return "";
    }

}
