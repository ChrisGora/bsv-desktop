package client;

import com.amazonaws.regions.Regions;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import javafx.concurrent.Task;
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

        UploadHolder upload = new UploadHolder();
        upload.setFile(file);
        upload.setMetadata(metadata);

        id = metadata.getId();

        if (id == null) {
            System.out.println("Image ID was null");
//            if (file.getName().contains("_E")) {
//                upload.onFailure("Image ID was null");
//                return upload;

                // FIXME: 23/07/18 onFailure called before the upload is registered with JavaFX

//            }

//            else {
                id = UUID.randomUUID().toString().replace("-", "");
//            }
        }

        String key = id + "-" + file.getName();
        System.out.println(key);

        upload.setKey(key);
        upload.setBucket(BUCKET);

//        Runnable storageConnection = new S3Connection(Regions.EU_WEST_2, upload);
        Runnable storageConnection = new LocalStorageConnection(upload);

//        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(storageConnection);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> SUBMITTED");
        upload.setCompletionListener(this::updateDatabase);

        return upload;
    }

    private void updateDatabase(UploadHolder upload) {  // TODO: 19/07/18 Supply the database with the metadata

        LocalDateTime localDateTime = LocalDateTime.now();

        executor.submit(() -> {
            try (DatabaseConnection rds = new DatabaseConnection()) {
                System.out.println("Uploader established an RDS connection");
//            System.out.println(upload.getBucket());
//            System.out.println(upload.getKey());
                ImageMetadata metadata = upload.getMetadata();
                rds.insertPhotoRow(
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}