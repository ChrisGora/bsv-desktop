package client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

class S3Connector {

    private AmazonS3 s3;
    private String bucketName;
    private String key;

    S3Connector(Regions region, String bucketName) {
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain();
        awsCredentialsProvider.getCredentials();
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        this.bucketName = bucketName;
    }

    public void listBuckets() {
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println("Found bucket: " + bucket.getName());
        }
    }

}
