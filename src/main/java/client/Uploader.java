package client;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import org.apache.commons.imaging.ImageReadException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Uploader {

    private static final String BUCKET = "bristol-streetview-photos";

    private ExecutorService executor;

    Uploader() {
        this.executor = Executors.newFixedThreadPool(8);
    }

//    public void test() {
//        s3Connection.listBuckets();
//    }

    public UploadHolder upload(File file) {
        String id = null;

        ImageMetadata metadata = null;
        try {
            metadata = new ImageMetadata(file);
        } catch (IOException | MetadataException | ImageProcessingException | ImageReadException e) {
            e.printStackTrace();
            return null;
        }
        id = metadata.getId();

        if (id == null) {
            System.out.println("Image ID was null");
            id = UUID.randomUUID().toString().replace("-", "");
        }

        String key = id + "-" + file.getName();
        System.out.println(key);

        UploadHolder upload = new UploadHolder();
        upload.setFile(file);
        upload.setMetadata(metadata);
        upload.setKey(key);
        upload.setBucket(BUCKET);

        S3Connection s3Connection = new S3Connection(Regions.EU_WEST_2, upload);
//        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(s3Connection::call);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> SUBMITTED");
        upload.setCompletionListener(this::updateDatabase);

        return upload;
    }

    private void updateDatabase(UploadHolder upload) {  // TODO: 19/07/18 Supply the database with the metadata
        executor.submit(() -> {
            try (RdsConnection rds = new RdsConnection()) {
                System.out.println("Uploader established an RDS connection");
//            System.out.println(upload.getBucket());
//            System.out.println(upload.getKey());
                ImageMetadata metadata = upload.getMetadata();
                rds.insertPhotoRow(
                        metadata.getId(),
                        metadata.getHeight(),
                        metadata.getWidth(),
                        new Timestamp((new Date()).getTime()),
                        new Timestamp(new Date().getTime()),
                        metadata.getLatitude(),
                        metadata.getLongitude(),
                        metadata.getSerialNumber(),
                        1,
                        upload.getBucket(),
                        upload.getKey()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}