package client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import sun.net.ProgressEvent;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class S3Connector {

    private AmazonS3 s3;
    private String bucketName;
    private String key;
    private ExecutorService executor;

    S3Connector(Regions region, String bucketName) {
//        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain();
//        awsCredentialsProvider.getCredentials();
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        this.bucketName = bucketName;

        executor = Executors.newFixedThreadPool(4);
    }

    public void listBuckets() {
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println("Found bucket: " + bucket.getName());

        }
    }

    // Needs to upload a photo and return some sort of reference (a url?)
    public void uploadFile(String key, File file) {
        executor.submit(() -> {
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
            request.setGeneralProgressListener((progressEvent) -> {
                ProgressEventType type = progressEvent.getEventType();
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
