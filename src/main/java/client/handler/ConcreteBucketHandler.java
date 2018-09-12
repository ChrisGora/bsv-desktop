package client.handler;

import client.databaseConnections.ImageMetadata;
import client.PhotoSet;
import client.databaseConnections.DatabaseConnection;
import client.storageConnections.LocalStorageConnection;
import client.storageConnections.S3Connection;
import client.storageConnections.StorageConnection;
import client.storageConnections.StorageType;
import client.util.Log;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.gson.Gson;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.WayPoint;
import io.jenetics.jpx.geom.Geoid;
import org.apache.commons.imaging.ImageReadException;
import rx.Observable;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
//import java.util.*;
import java.util.*;
import java.util.List;
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
public class ConcreteBucketHandler implements BucketHandler {

//    TODO: 09/08/18 Implement logic to keep track of routeIds
//    Get the current highest route id from the database when the object is instantiated
//    Provide a new 'increment rout id' method to increment the id manually - eg by the UI

    private static final String TAG = "ConcreteBucketHandler";

    private final double searchRadiusMeters;
    private final String bucket;
    private final StorageType type;
    private ExecutorService executor;
    private SpatialDatabaseConnection spatialDatabaseConnection;
    private List<FileHolder> doneUploads;

    public ConcreteBucketHandler(String bucket, StorageType type) {
        this(bucket, type, 5000);
    }

    public ConcreteBucketHandler(String bucket, StorageType type, double searchRadiusMeters) {
        this.type = type;
        this.bucket = bucket;
        this.searchRadiusMeters = searchRadiusMeters;
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

    @Override
    public void close() throws IOException {
        spatialDatabaseConnection.close();
        executor.shutdown();
    }

    @Override
    public FileHolder newFileHolder(File file) {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setFile(file);
        fileHolder.setBucket(bucket);
        return fileHolder;
    }

    @Override
    public FileHolder newEmptyFileHolder() {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setBucket(bucket);
        return fileHolder;
    }

    @Override
    public void upload(FileHolder upload) {
        upload(upload, 0);
    }

    @Override
    public void upload(FileHolder upload, int routeNumber) {
        ImageMetadata metadata = getImageMetadata(upload, routeNumber);
        if (metadata == null) return;
        else upload.setMetadata(metadata);

        String id = getId(upload, metadata);
        if (id == null) return;

        upload.setKey(getKey(upload, id));

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

    private ImageMetadata getImageMetadata(FileHolder upload, int routeNumber) {
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
                metadata = new ImageMetadata(upload.getFile(), json, routeNumber);
            } else {
                metadata = new ImageMetadata(upload.getFile(), routeNumber);
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
                Log.v(TAG, "Established a DB connection");
                ImageMetadata metadata = upload.getMetadata();
                int result = db.insertPhotoRow(
                        metadata,
                        localDateTime,
                        metadata.getRouteNumber(),
                        upload.getBucket(),
                        upload.getKey()
                );

                if (result == 1) {
                    spatialDatabaseConnection.add(metadata.getId(), metadata.getLatitude(), metadata.getLongitude());
                    upload.onDbSuccess();
                } else {
                    upload.onDbFailure("Database error - database returned: " + result);
                    removeFromDatabase(upload);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                upload.onDbFailure(e.toString());
                removeFromDatabase(upload);
            }
        });
    }

    private void removeFromDatabase(FileHolder upload) {
        upload.setRemoveCompletionListener((f) -> Log.w(TAG, "REMOVE Done: " + f.getKey()));
        upload.setRemoveFailureListener((error) -> Log.e(TAG, error));

        StorageConnection storageConnection = getStorageConnection(upload);
        executor.submit(storageConnection::removeFile);
    }

    @Override
    public int saveJustUploadedAsNewRoute(int routeId) {
        List<WayPoint> wayPoints = getWayPoints();
        GPX gpx = getGpx(wayPoints);
        uploadGpx(routeId, gpx);
        return wayPoints.size();
    }

    private void uploadGpx(int routeId, GPX gpx) {
        try {
            File tempFile = File.createTempFile("JPX" + routeId, ".gpx");
            tempFile.deleteOnExit();

            GPX.write(gpx, tempFile.getPath());

            FileHolder fileHolder = newGpxFileHolder(routeId, tempFile);

            fileHolder.setUploadCompletionListener(f -> {
                Log.v(TAG, "GPX UPLOAD DONE");
                doneUploads.clear();
                tempFile.delete();
            });

            fileHolder.setUploadFailureListener((error) -> Log.e(TAG, "GPX file upload failure"));

            StorageConnection storageConnection = getStorageConnection(fileHolder);
            executor.submit(storageConnection::copyFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileHolder newGpxFileHolder(int routeId, File tempFile) {
        FileHolder fileHolder = newFileHolder(tempFile);
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

    @Override
    public void deleteAll() {
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.deleteAll(bucket);
            FileHolder bucketHolder = new FileHolder();
            bucketHolder.setBucket(bucket);
            StorageConnection storageConnection = getStorageConnection(bucketHolder);
            storageConnection.removeAll();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

// --------------------------------------------------------------------------------------------------------------------

    @Override
    public void savePhotosAround(double latitude, double longitude, int maxResults) throws IOException {
        int i = 1;
        PhotoSet set = getPhotosAround(latitude, longitude, maxResults);
        for (String id : set.getIds()) {
            FileHolder outputHolder = newEmptyFileHolder();
            String fileKey = null;
            try (DatabaseConnection db = new DatabaseConnection()) {
                fileKey = db.getPath(id).getKey();
                Objects.requireNonNull(fileKey, "Retrieved file key was null");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            StorageConnection storageConnection = getStorageConnection(outputHolder);
            outputHolder.setFile(storageConnection.getFile(fileKey));
            outputHolder.setKey(i + fileKey.substring(fileKey.lastIndexOf('.')));
            System.out.println(outputHolder.getFile());
            System.out.println(outputHolder.getKey());

            executor.submit(storageConnection::copyFileToOutput);

            File tempFile = File.createTempFile("bsv", ".json");
//            tempFile.deleteOnExit();

            PhotoResult photoInfo = new PhotoResult(set.getImages().get(id), set.getDistances().get(id));

            Gson gson = new Gson();

            try (Writer writer = new FileWriter(tempFile)) {
                gson.toJson(photoInfo, writer);
            }



            outputHolder = newFileHolder(tempFile);
            File file = outputHolder.getFile();


            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(">>>>>>>>>>>>>>");
                System.out.println(line);
            }

            outputHolder.setBucket(bucket);
            outputHolder.setKey(i + ".json");

            storageConnection = getStorageConnection(outputHolder);
            executor.submit(storageConnection::copyFileToOutput);

            i++;
        }
    }

    @Override
    public void savePhotosTakenOn(LocalDateTime dateTime) {

    }

    @Override
    public void savePhotosUploadedOn(LocalDateTime dateTime) {

    }

    @Override
    public void savePhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {

    }

    @Override
    public void savePhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {

    }

    @Override
    public PhotoSet getPhotosTakenOn(LocalDateTime dateTime) {
        return null;
    }

    @Override
    public PhotoSet getPhotosUploadedOn(LocalDateTime dateTime) {
        return null;
    }

    @Override
    public PhotoSet getPhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return null;
    }

    @Override
    public PhotoSet getPhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return null;
    }

    @Nullable
    @Override
    public PhotoSet getPhotosAround(double latitude, double longitude, int maxResults) {
        List<ImageMetadata> images = null;
        try {
            List<String> ids = spatialDatabaseConnection.getUnsortedImageIds(latitude, longitude, searchRadiusMeters, maxResults);
            System.out.println(">>>>>>> UNSORTED IDS:");
            System.out.println(ids.size());
            System.out.println(ids);
            images = getListOfMetadata(ids);
            System.out.println(images);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (images == null) {
            throw new IllegalStateException("Images shouldn't be null");
        } else {
            return getPhotoSet(latitude, longitude, images);
        }

    }

    private PhotoSet getPhotoSet(double latitude, double longitude, List<ImageMetadata> images) {
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

    private List<ImageMetadata> getListOfMetadata(List<String> ids) throws SQLException {
        List<ImageMetadata> metadataList = new ArrayList<>();
        try (DatabaseConnection db = new DatabaseConnection()) {
            for (String id : ids) {
                ImageMetadata metadata = db.getMetadata(id);
                if (metadata != null) metadataList.add(metadata);
                else throw new IllegalStateException("ID not found in the database");
            }
        }
        return metadataList;

        // TODO: 31/08/18 Test me

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


    private class SpatialDatabaseConnection implements AutoCloseable {

        private RTree<String, Geometry> tree;

        SpatialDatabaseConnection() {
            this.tree = getRTree();
        }

        @Override
        public void close() throws IOException {
            saveRTree();
        }

        void saveRTree() throws IOException {
            FileHolder fileHolder = new FileHolder();
            fileHolder.setBucket(bucket);
            getStorageConnection(fileHolder).saveRTree(tree);
        }

        private RTree<String, Geometry> getRTree() {
            FileHolder fileHolder = newEmptyFileHolder();
            Optional<RTree<String, Geometry>> optionalTree = getStorageConnection(fileHolder).getRTree();
            return optionalTree.orElseGet(this::newRTree);
        }

        private RTree<String, Geometry> newRTree() {
            return RTree.create();
        }

        synchronized void add(String id, double latitude, double longitude) {
            Log.v(TAG, "add: ADDING TO THE TREE");
            this.tree = tree.add(id, Geometries.point(latitude, longitude));
            Log.d(TAG, tree.asString());
        }

        List<String> getUnsortedImageIds(double latitude, double longitude, double searchRadiusMeters) {
            return getUnsortedImageIds(latitude, longitude, searchRadiusMeters, 100);
        }

        List<String> getUnsortedImageIds(double latitude, double longitude, double searchRadiusMeters, int maxResults) {
            List<String> ids = new ArrayList<>();
            Observable<Entry<String, Geometry>> searchResult = search(Geometries.point(latitude, longitude), searchRadiusMeters);
            Objects.requireNonNull(searchResult, "Search result was null");

            searchResult.forEach((entry) -> ids.add(entry.value()));

            System.out.println(ids);
            return ids;
        }

        @Nullable
        private Observable<Entry<String, Geometry>> search(Point latLon, final double distanceMeters) {
            // First we need to calculate an enclosing lat long rectangle for this
            // distance then we refine on the exact distance

            double distanceKm = distanceMeters / 1000;
            Position from = Position.create(latLon.x(), latLon.y());
            Rectangle bounds = createBounds(from, distanceKm);

            System.out.println(tree.asString());
            return tree
                    // do the first search using the bounds
//                    .search(bounds)
                    .search(latLon, distanceKm)
                    // refine using the exact distance
                    .filter(entry -> {
                        Geometry geometry = entry.geometry();
                        if (geometry instanceof Point) {
                            Point point  = (Point) geometry;
                            return isWithinRange(distanceKm, from, Position.create(point.x(), point.y()));
                        } else {
                            return false;
                        }
                    });
        }

        private boolean isWithinRange(double radiusKm, Position a, Position b) {
            return a.getDistanceToKm(b) < (radiusKm);
        }

        private Rectangle createBounds(final Position from, final double distanceKm) {
            // this calculates a pretty accurate bounding box. Depending on the
            // performance you require you wouldn't have to be this accurate because
            // accuracy is enforced later
            Position north = from.predict(distanceKm, 0);
            Position south = from.predict(distanceKm, 180);
            Position east = from.predict(distanceKm, 90);
            Position west = from.predict(distanceKm, 270);

            return Geometries.rectangle(west.getLon(), south.getLat(), east.getLon(), north.getLat());
        }

    }

}