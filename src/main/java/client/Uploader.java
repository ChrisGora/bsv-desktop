package client;

import com.amazonaws.regions.Regions;

import java.io.File;
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

        ImageMetadata metadata = new ImageMetadata(file);
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

        System.out.println("WTF");
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

            // FIXME: 19/07/18 Don't run the database connection on the main thread!!!!!!!

//            if (result == 1) {
//                System.out.println("Database Update Successful");
//            }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}