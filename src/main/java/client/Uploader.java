package client;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import io.jenetics.jpx.geom.Geoid;
import org.apache.commons.imaging.ImageReadException;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Uploader {

    private static final double LATITUDE_DELTA = 0.001;
    private static final double LONGITUDE_DELTA = 0.001;

    private final String bucket;
    private final StorageType type;

    private ExecutorService executor;
    private List<FileHolder> doneUploads;

    Uploader(StorageType type, String bucket) {
//        this.executor = Executors.newFixedThreadPool(2);
        this.executor = new DebuggingExecutor(2, 2, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
        this.doneUploads = new ArrayList<>();
        this.type = type;
        this.bucket = bucket;
    }

    private StorageConnection getStorageConnection(FileHolder fileHolder) {
        switch (type) {
            case AMAZON:
                return new S3Connection(fileHolder);

            case LOCAL:
                return new LocalStorageConnection(fileHolder);

                default:
                    return new LocalStorageConnection(fileHolder);
        }
    }

    public void stop() {
        executor.shutdown();
    }

    public FileHolder newFileHolder(File file) {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setFile(file);
        return fileHolder;
    }

    public void upload(FileHolder upload) {

        ImageMetadata metadata = null;
        try {
            metadata = new ImageMetadata(upload.getFile());
        } catch (IOException | MetadataException | ImageProcessingException | ImageReadException e) {
            e.printStackTrace();
            upload.onUploadFailure(e.toString());
            return;
        }

//        FileHolder upload = new FileHolder();
//        upload.setFile(file);
        upload.setMetadata(metadata);

        String id = null;
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
        upload.setBucket(bucket);

//        Runnable storageConnection = new S3Connection(upload);
        StorageConnection storageConnection = getStorageConnection(upload);

//        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(storageConnection::copyFile);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> SUBMITTED");
        upload.setUploadCompletionListener(this::updateDatabase);
        upload.setUploadCompletionListener(doneUploads::add);
//        return upload;
    }

    private void updateDatabase(FileHolder upload) {

        LocalDateTime localDateTime = LocalDateTime.now();

        executor.submit(() -> {
            try (DatabaseConnection db = new DatabaseConnection()) {
                System.out.println("Uploader established a DB connection");
//            System.out.println(upload.getBucket());
//            System.out.println(upload.getKey());
                ImageMetadata metadata = upload.getMetadata();
                int result = db.insertPhotoRow(
                        metadata,
                        localDateTime,
                        1,
                        upload.getBucket(),
                        upload.getKey()
                );

                if (result == 1) {
                    upload.onDbSuccess();
                } else {
                    upload.onDbFailure("Database error - database returned: " + result);

                    upload.setRemoveCompletionListener((f) -> System.out.println("REMOVE Done: " + f.getKey()));
                    upload.setRemoveFailureListener(System.out::println);

                    StorageConnection storageConnection = getStorageConnection(upload);
                    executor.submit(storageConnection::removeFile);
                    // TODO: 23/07/18 Remove the photo from file storage if it was rejected by the database

                }

            } catch (SQLException e) {
                e.printStackTrace();
                upload.onDbFailure(e.toString());

                upload.setRemoveCompletionListener((f) -> System.out.println("REMOVE Done: " + f.getKey()));
                upload.setRemoveFailureListener(System.out::println);

                StorageConnection storageConnection = getStorageConnection(upload);
                executor.submit(storageConnection::removeFile);

                // FIXME: 31/07/18 Duplicate code for removing dead files
            }
        });
    }

    public void saveJustUploadedAsNewRoute(int routeId) {

        List<WayPoint> wayPoints = getWayPoints();
        GPX gpx = getGpx(wayPoints);

        System.out.println("Waypoints size: " + wayPoints.size());
        System.out.println("builder done");
        uploadGpx(routeId, gpx);
    }

    private void uploadGpx(int routeId, GPX gpx) {
        try {
            System.out.println("inside try");
            File tempFile = File.createTempFile("JPX" + routeId, ".gpx");
            GPX.write(gpx, tempFile.getPath());

            FileHolder fileHolder = newGpxFileHolder(routeId, tempFile);

            fileHolder.setUploadCompletionListener(f -> {
                System.out.println("GPX UPLOAD DONE!!!");
                doneUploads.clear();
                tempFile.delete();
            });

            fileHolder.setProgressListener(System.out::println);
            fileHolder.setUploadFailureListener(System.out::println);

            StorageConnection storageConnection = getStorageConnection(fileHolder);
            executor.submit(storageConnection::copyFile);

            System.out.println("submited");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileHolder newGpxFileHolder(int routeId, File tempFile) {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setFile(tempFile);
        fileHolder.setBucket(bucket);
        fileHolder.setKey("GPX_" + routeId + "_" + UUID.randomUUID().toString().replace("-", "") + ".gpx");
        return fileHolder;
    }

    private GPX getGpx(List<WayPoint> wayPoints) {
        return GPX.builder()
                    .addTrack(t -> t
                    .addSegment(s -> wayPoints.forEach(s::addPoint)))
                    .build();
    }

    private List<WayPoint> getWayPoints() {
        List<WayPoint> wayPoints = new ArrayList<>();

        System.out.println("done size: " + doneUploads.size());

        for (FileHolder fileHolder : doneUploads) {
            double longitude = fileHolder.getMetadata().getLongitude();
            double latitude = fileHolder.getMetadata().getLatitude();
            Instant instant = fileHolder.getMetadata().getPhotoDateTime().toInstant(ZoneOffset.ofTotalSeconds(0));
            WayPoint wayPoint = WayPoint.builder().lon(longitude).lat(latitude).time(instant).build();
            wayPoints.add(wayPoint);
        }
        return wayPoints;
    }

    public void deleteAll() {
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll(bucket);
            FileHolder bucketHolder = new FileHolder();
            bucketHolder.setBucket(bucket);
            StorageConnection storageConnection = getStorageConnection(bucketHolder);
            storageConnection.removeAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public PhotoSet getPhotos(double latitude, double longitude) {
        List<ImageMetadata> images = null;
        try (DatabaseConnection db = new DatabaseConnection()) {
            images = db.getPhotosAround(latitude, LATITUDE_DELTA, longitude, LONGITUDE_DELTA);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (images == null) {
            throw new IllegalStateException("Images shouldn't be null");
        } else {

            List<String> ids = new ArrayList<>();
            List<Double> distances = new ArrayList<>();

            for (ImageMetadata image : images) {

                Point end = WayPoint.of(latitude, longitude);
                Point start = WayPoint.of(image.getLatitude(), image.getLongitude());
                Length distance = Geoid.WGS84.distance(start, end);

                ids.add(image.getId());
                distances.add(distance.doubleValue());
            }




//            PhotoSet photoSet = new PhotoSet(latitude, longitude);
        }

    }
}