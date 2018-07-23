package client;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import org.apache.commons.imaging.ImageReadException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Uploader {

    private static final String BUCKET = "bristol-streetview-photos";

    private ExecutorService executor;

    Uploader() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void stop() {
        executor.shutdown();
    }

//    public void test() {
//        s3Connection.listBuckets();
//    }

    public UploadHolder newUploadHolder(File file) {
        UploadHolder uploadHolder = new UploadHolder();
        uploadHolder.setFile(file);
        return uploadHolder;
    }


    // TODO: 23/07/18 Change the signature: void upload(UploadHolder)

    public void upload(UploadHolder upload) {
        String id = null;

        ImageMetadata metadata = null;
        try {
            metadata = new ImageMetadata(upload.getFile());
        } catch (IOException | MetadataException | ImageProcessingException | ImageReadException e) {
            e.printStackTrace();
            upload.onUploadFailure(e.toString());
            return;
        }

//        UploadHolder upload = new UploadHolder();
//        upload.setFile(file);
        upload.setMetadata(metadata);

        id = metadata.getId();


        if (id == null) {
//            System.out.println("Image ID was null");
            if (upload.getFile().getName().contains("_E")) {
                upload.onUploadFailure("Image ID was null");
                return;
            }

            else {
                id = UUID.randomUUID().toString().replace("-", "");
                metadata.setId(id);
            }
        }

        String key = id + "-" + upload.getFile().getName();
        System.out.println(key);

        upload.setKey(key);
        upload.setBucket(BUCKET);

//        Runnable storageConnection = new S3Connection(Regions.EU_WEST_2, upload);
        Runnable storageConnection = new LocalStorageConnection(upload);

//        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(storageConnection);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> SUBMITTED");
        upload.setUploadCompletionListener(this::updateDatabase);

//        return upload;
    }

    private void updateDatabase(UploadHolder upload) {  // TODO: 19/07/18 Supply the database with the metadata

        LocalDateTime localDateTime = LocalDateTime.now();

        executor.submit(() -> {
            try (DatabaseConnection rds = new DatabaseConnection()) {
                System.out.println("Uploader established an RDS connection");
//            System.out.println(upload.getBucket());
//            System.out.println(upload.getKey());
                ImageMetadata metadata = upload.getMetadata();
                int result = rds.insertPhotoRow(
                        metadata.getId(),
                        metadata.getHeight(),
                        metadata.getWidth(),
//                        new Timestamp((new Date()).getTime()),
//                        new Timestamp(new Date().getTime()),
                        metadata.getPhotoDateTime(),
                        localDateTime,
                        metadata.getLatitude(),
                        metadata.getLongitude(),
                        metadata.getSerialNumber(),
                        1,
                        upload.getBucket(),
                        upload.getKey()
                );

                if (result == 1) {
                    upload.onDbSuccess();
                } else {
                    upload.onDbFailure("Database error - database returned: " + result);

                    // TODO: 23/07/18 Remove the photo from file storage if it was rejected by the database

                }

            } catch (Exception e) {
                e.printStackTrace();
                upload.onDbFailure(e.toString());
            }
        });
    }

}