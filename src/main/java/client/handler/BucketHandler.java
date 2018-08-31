package client.handler;

import client.databaseConnections.ImageMetadata;
import client.PhotoSet;
import client.databaseConnections.DatabaseConnection;
import client.storageConnections.LocalStorageConnection;
import client.storageConnections.S3Connection;
import client.storageConnections.StorageConnection;
import client.storageConnections.StorageType;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.WayPoint;
import io.jenetics.jpx.geom.Geoid;
import org.apache.commons.imaging.ImageReadException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
//import java.util.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manipulates a bucket storing the 360 photos and the associated database.
 * Adds new photos, removes photos, and retrieves photos based on specified criteria.
 * {@link StorageType} enum specifies where the bucket might be located.
 * Behaviour of this class is identical for all Storage Types.
 *
 * Any SQL specific classes, such as ResultSet, should never be exposed as return values.
 *
 * @author Chris Gora
 * @version 1.0, 01.09.2018
 */
public class BucketHandler implements AutoCloseable {

//    TODO: 09/08/18 Implement logic to keep track of routeIds
//    Get the current highest route id from the database when the object is instantiated
//    Provide a new 'increment rout id' method to increment the id manually - eg by the UI

    private final double latitudeDelta;
    private final double longitudeDelta;
    private final String bucket;
    private final StorageType type;
    private ExecutorService executor;
    private SpatialDatabaseConnection spatialDatabaseConnection;
    private List<FileHolder> doneUploads;

    public BucketHandler(String bucket, StorageType type) {
        this(bucket, type, 0.001, 0.001);
    }

    public BucketHandler(String bucket, StorageType type, double latitudeDelta, double longitudeDelta) {
        this.type = type;
        this.bucket = bucket;
        this.latitudeDelta = latitudeDelta;
        this.longitudeDelta = longitudeDelta;
        this.spatialDatabaseConnection = new SpatialDatabaseConnection();
        // FIXME: 09/08/18 Remove the debugging executor and use standard executor for real life
//        this.executor = new DebuggingExecutor(2, 2, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
        this.executor = Executors.newWorkStealingPool(4);
        this.doneUploads = new ArrayList<>();

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

//    @Deprecated
//    public void stop() throws Exception {
//        close();
//    }

    @Override
    public void close() {
        executor.shutdown();
    }

    public FileHolder newFileHolder(File file) {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setFile(file);
//        fileHolder.setBucket(bucket);
        return fileHolder;
    }

    public FileHolder newEmptyFileHolder() {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setBucket(bucket);
        return fileHolder;
    }

    public void upload(FileHolder upload) {
        ImageMetadata metadata = getImageMetadata(upload);
        if (metadata == null) return;
        else upload.setMetadata(metadata);

        System.out.println(metadata);

        String id = getId(upload, metadata);
        if (id == null) return;

        upload.setKey(getKey(upload, id));
        upload.setBucket(bucket);

        StorageConnection storageConnection = getStorageConnection(upload);
        executor.submit(storageConnection::copyFile);
        upload.setUploadCompletionListener(this::updateDatabase);
        upload.setUploadCompletionListener(doneUploads::add);
    }

    private String getKey(FileHolder upload, String id) {
        return id + "-" + upload.getFile().getName();
    }

    private String getId(FileHolder upload, ImageMetadata metadata) {
        String id = null;
        id = metadata.getId();

        if (id == null) {
//            System.out.println("Image ID was null");
            if (upload.getFile().getName().contains("_E")) {
                upload.onUploadFailure("Image ID was null");
                return null;
            }

            else {
                id = UUID.randomUUID().toString().replace("-", "");
                metadata.setId(id);
            }
        }
        return id;
    }

    private ImageMetadata getImageMetadata(FileHolder upload) {
        ImageMetadata metadata = null;
        try {
            String jsonPath = null;
            if (upload.getFile().getName().contains("_E.jpg")) {
                jsonPath = upload.getFile().getAbsolutePath().replace("_E.jpg", "_I.json");
            } else if (upload.getFile().getName().contains(".jpg")) {
                jsonPath = upload.getFile().getAbsolutePath().replace(".jpg", "_I.json");
            }

            File json = null;

            if (jsonPath != null) {
                json = new File(jsonPath);
            }

            if (json != null && json.exists()) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> JSON EXISTS");
                metadata = new ImageMetadata(upload.getFile(), json);
            } else {
                System.out.println("HERERERERE");
                metadata = new ImageMetadata(upload.getFile());
            }
            return metadata;
        } catch (IOException | MetadataException | ImageProcessingException | ImageReadException e) {
            e.printStackTrace();
            upload.onUploadFailure(e.toString());
            return null;
        }
    }

    private void updateDatabase(FileHolder upload) {

        LocalDateTime localDateTime = LocalDateTime.now();

        executor.submit(() -> {
            try (DatabaseConnection db = new DatabaseConnection()) {
                System.out.println("BucketHandler established a DB connection");
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

    public int saveJustUploadedAsNewRoute(int routeId) {

        List<WayPoint> wayPoints = getWayPoints();
        GPX gpx = getGpx(wayPoints);

        System.out.println("Waypoints size: " + wayPoints.size());
        System.out.println("builder done");
        uploadGpx(routeId, gpx);
        return wayPoints.size();
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

        doneUploads.sort(Comparator.comparing(file -> file.getMetadata().getPhotoDateTime()));

        for (FileHolder fileHolder : doneUploads) {

            // FIXME: 13/08/18 Getter call on a getter!

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
            images = db.getPhotosAround(latitude, latitudeDelta, longitude, longitudeDelta);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (images == null) {
            throw new IllegalStateException("Images shouldn't be null");
        } else {
            List<PhotoResult> photoResults = new ArrayList<>();

            for (ImageMetadata image : images) {
                final WayPoint wayPoint = WayPoint.of(latitude, longitude);
                final Length distance = Geoid.WGS84.distance(WayPoint.of(image.getLatitude(), image.getLongitude()), wayPoint);
                photoResults.add(new PhotoResult(image, distance.doubleValue()));
            }

            photoResults.sort(Comparator.comparingDouble(PhotoResult::getDistance));
            PhotoSet photoSet = new PhotoSet(latitude, longitude);

            for (PhotoResult result : photoResults) {
                photoSet.add(result.getImageMetadata(), result.getDistance());
            }
            return photoSet;
        }

    }

    private class PhotoResult {

        private final String id;
        private final double distance;
        private final ImageMetadata imageMetadata;

        PhotoResult(ImageMetadata imageMetadata, double distance) {
            this.id = imageMetadata.getId();
            this.distance = distance;
            this.imageMetadata = imageMetadata;
        }

        String getId() {
            return id;
        }


        double getDistance() {
            return distance;
        }

        ImageMetadata getImageMetadata() {
            return imageMetadata;
        }
    }

    private class SpatialDatabaseConnection {

        private RTree<String, Geometry> tree;

        SpatialDatabaseConnection() {
            this.tree = getRTree();
        }

        private RTree<String, Geometry> getRTree() {
            FileHolder fileHolder = new FileHolder();
            fileHolder.setBucket(bucket);
            Optional<RTree<String, Geometry>> optionalTree = getStorageConnection(fileHolder).getRTree();
            return optionalTree.orElseGet(this::newRTree);
        }

        private RTree<String, Geometry> newRTree() {
            return RTree.create();
        }

        void add(String id, double latitude, double longitude) {
            tree = tree.add(id, Geometries.point(latitude, longitude));
        }

        List<String> get(double latitude, double longitude, double deltaMeters) {
            return get(latitude, longitude, deltaMeters, 100);
        }

        List<String> get(double latitude, double longitude, double deltaMeters, int maxResults) {
            throw new RuntimeException("Implement me");
        }

/*        private Observable<Entry<String, Geometry>> search(RTree<String, Geometry> tree, Point lonLat, final double distanceMeters) {
            // First we need to calculate an enclosing lat long rectangle for this
            // distance then we refine on the exact distance
            final Position from = Position.create(lonLat.y(), lonLat.x());
            // FIXME: 31/08/18
//            Rectangle bounds = createBounds(from, distanceMeters);
            return tree
                    // do the first search using the bounds
                    .search(bounds)
                    // refine using the exact distance
                    .filter(new Func1<Entry<String, Geometry>, Boolean>() {
                        @Override
                        public Boolean call(Entry<String, Geometry> entry) {
                            Geometry p = entry.geometry();
//                            Point p2;
                            // FIXME: 31/08/18 Cast the geometry to a point?
//                            Position position = Position.create(p.y(), p.x());
//                            return from.getDistanceToKm(position) < (distanceMeters / 1000);
                        }
                    });
        }*/

        void saveRTree() {

//            getStorageConnection()
        }
    }

}