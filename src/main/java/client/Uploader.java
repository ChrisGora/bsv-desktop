package client;

import com.adobe.xmp.XMPException;
import com.amazonaws.regions.Regions;
import com.drew.imaging.ImageProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Uploader {

    private String bucket = "bristol-streetview-photos";

    Uploader() {
    }

//    public void test() {
//        s3Connection.listBuckets();
//    }

    public UploadHolder upload(File file) {
        String id = null;

        try {
            ImageMetadata metadata = new ImageMetadata(file);
            id = metadata.getId();
            metadata.printMetadata();
        } catch (ImageProcessingException | IOException | XMPException e) {
            e.printStackTrace();
        }

        if (id == null) {
            System.out.println("Image ID was null");
            id = UUID.randomUUID().toString().replace("-", "");
        }

        String key = id + "-" + file.getName();
        System.out.println(key);

        UploadHolder upload = new UploadHolder();
        upload.setFile(file);
        upload.setKey(key);
        upload.setBucket(bucket);

//        S3Connection s3Connection = new S3Connection(Regions.EU_WEST_2, upload);
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(s3Connection);

        upload.setCompletionListener(this::updateDatabase);

        return upload;
    }

    private void updateDatabase(UploadHolder upload) {
        try (RdsConnection rds = new RdsConnection()) {
            System.out.println("Uploader established an RDS connection");
            System.out.println(upload.getBucket());
            System.out.println(upload.getKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}