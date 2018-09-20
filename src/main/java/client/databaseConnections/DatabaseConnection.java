package client.databaseConnections;

import client.util.Log;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseConnection implements AutoCloseable {

    private static final String TAG = "DatabaseConnection";
    //    private static final Region REGION = Region.getRegion(Regions.EU_WEST_2);
    private static final Regions REGION = Regions.EU_WEST_2;
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 3306;
    private static final String JDBC_URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/bristol_streetview_schema";
    private static final String USERNAME = "java-db-client";
    private static final String PASSWORD = "Re278nErRowD";

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_PROVIDER = "SUN";
    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

    private static final String SSL_CERTIFICATE = "rds-ca-2015-eu-west-2.pem";

    private Connection connection;

    public DatabaseConnection() throws SQLException {
        try {
            this.connection = newConnection();
            testConnection();
        } catch (IOException | CertificateException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
    }

    private Connection newConnection() throws SQLException, IOException, CertificateException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Properties info = newMySqlProperties();
        return DriverManager.getConnection(JDBC_URL, info);
    }

    private void testConnection() throws SQLException {
        String sql = "SELECT 'Success!' FROM DUAL;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String output = results.getString(1);
                if (!output.equals("Success!")) {
                    throw new SQLException("Database Success Message not received");
                } else {
                    Log.v(TAG, "DB Connection successfully opened");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Properties newMySqlProperties() {
        Properties mysqlProperties = new Properties();

        mysqlProperties.setProperty("verifyServerCertificate", "true");
        mysqlProperties.setProperty("useSSL", "false");
        mysqlProperties.setProperty("user", USERNAME);
        mysqlProperties.setProperty("password", PASSWORD);

        return mysqlProperties;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
        Log.v(TAG, "DB Connection closed");
    }

    public int insertPhotoRow(
            ImageMetadata metadata,
            LocalDateTime uploadDateTime,
            int routeId,
            String bucketName,
            String key
    ) throws SQLException {

        return insertPhotoRow(
                metadata.getId(),
                metadata.getHeight(),
                metadata.getWidth(),
                metadata.getPhotoDateTime(),
                uploadDateTime,
                metadata.getLatitude(),
                metadata.getLongitude(),
                metadata.getSerialNumber(),
                routeId,
                bucketName,
                key,
                metadata.getLocationAccuracy(),
                metadata.getBearing(),
                metadata.getBearingAccuracy()
        );
    }

    @VisibleForTesting
    protected int insertPhotoRow(String id,
                                 int height,
                                 int width,
                                 LocalDateTime photoDateTime,
                                 LocalDateTime uploadDateTime,
                                 Double latitude,
                                 Double longitude,
                                 String cameraSerialNumber,
                                 int routeId,
                                 String bucketName,
                                 String key,
                                 double locationAccuracy,
                                 double bearing,
                                 double bearingAccuracy
    ) throws SQLException {

        String sql = "INSERT INTO Photo " +
                "(id, height, width, photoTimestamp, uploadTimestamp, latitude, longitude, cameraSerialNumber, routeId, bucketName, fileKey, locationAccuracy, bearing, bearingAccuracy) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int n = -1;

        Timestamp photoTimestamp = Timestamp.valueOf(photoDateTime);
        Timestamp uploadTimeStamp = Timestamp.valueOf(uploadDateTime);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.setInt(2, height);
            statement.setInt(3, width);
            statement.setTimestamp(4, photoTimestamp);
            statement.setTimestamp(5, uploadTimeStamp);
            statement.setDouble(6, latitude);
            statement.setDouble(7, longitude);
            statement.setString(8, cameraSerialNumber);
            statement.setInt(9, routeId);
            statement.setString(10, bucketName);
            statement.setString(11, key);
            statement.setDouble(12, locationAccuracy);
            statement.setDouble(13, bearing);
            statement.setDouble(14, bearingAccuracy);

            n = statement.executeUpdate();

        }

        return n;

    }

    public ImageMetadata getMetadata(String id) throws SQLException {
        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE id = ? ";

        ImageMetadata metadata;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();

            results.first();
            metadata = newImageMetadata(results);
        }

        return metadata;
    }

    public FilePath getPath(String id) throws SQLException {
        String sql = "SELECT bucketName, fileKey FROM Photo " +
                "WHERE (id = ?);";

        String bucket = null;
        String key = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();

            results.first();
            bucket = results.getString("bucketName");
            key = results.getString("fileKey");
        }

        if (bucket == null || key == null) throw new SQLException("Bucket or key was null");
        else {
            FilePath filePath = new FilePath();
            filePath.setBucket(bucket);
            filePath.setKey(key);
            return filePath;
        }

    }

    public List<ImageMetadata> getPhotosTakenAt(double latitude, double longitude) throws SQLException {
        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE latitude = ? " +
                "AND longitude = ?;";

        List<ImageMetadata> images = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, latitude);
            statement.setDouble(2, longitude);

            ResultSet results = statement.executeQuery();

            int n = 0;
            while (results.next()) {
                images.add(newImageMetadata(results));
                n++;
            }

            if (n == 0) throw new SQLException("ResultSet was empty");
        }
        return images;
    }

    private ImageMetadata newImageMetadata(ResultSet results) throws SQLException {
        return new ImageMetadata(
                results.getString("id"),
                results.getInt("height"),
                results.getInt("width"),
                LocalDateTime.ofInstant(results.getTimestamp("photoTimestamp").toInstant(), ZoneId.systemDefault()),
                results.getDouble("latitude"),
                results.getDouble("longitude"),
                results.getString("cameraSerialNumber"),
                results.getInt("routeId"),
                results.getDouble("bearing"),
                results.getDouble("bearingAccuracy"),
                results.getDouble("locationAccuracy")
        );
    }

    @Deprecated
    @VisibleForTesting
    public List<ImageMetadata> getPhotosTakenOn(LocalDateTime dateTime) throws SQLException {
        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE photoTimestamp = ?;";

        List<ImageMetadata> images = new ArrayList<>();
        executeSqlWithOneDate(sql, dateTime, images);
        return images;
    }

    @Deprecated
    @VisibleForTesting
    private void executeSqlWithOneDate(String sql, LocalDateTime dateTime, List<ImageMetadata> images) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(dateTime));

            ResultSet results = statement.executeQuery();

            int n = 0;
            while (results.next()) {
                images.add(newImageMetadata(results));
                n++;
            }

            if (n == 0) throw new SQLException("ResultSet was empty");
        }
    }

    @Deprecated
    @VisibleForTesting
    public List<ImageMetadata> getPhotosUploadedOn(LocalDateTime dateTime) throws SQLException {
        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE uploadTimestamp = ?;";

        List<ImageMetadata> images = new ArrayList<>();
        executeSqlWithOneDate(sql, dateTime, images);
        return images;
    }

    @Deprecated
    @VisibleForTesting
    public List<ImageMetadata> getPhotosTakenBetween(LocalDateTime date1, LocalDateTime date2) throws SQLException {
        return getPhotosBetween(date1, date2, "photoTimestamp");
    }

    @Deprecated
    @VisibleForTesting
    private List<ImageMetadata> getPhotosBetween(LocalDateTime date1, LocalDateTime date2, String where) throws SQLException {
        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE " + where + " BETWEEN ? AND ?;";
        List<ImageMetadata> images = new ArrayList<>();
        executeSqlWithTwoDates(sql, date1, date2, images);
        return images;
    }

    @Deprecated
    @VisibleForTesting
    private void executeSqlWithTwoDates(String sql, LocalDateTime dateTime1, LocalDateTime dateTime2, List<ImageMetadata> images) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
//            statement.set(1, where);
            statement.setTimestamp(1, Timestamp.valueOf(dateTime1));
            statement.setTimestamp(2, Timestamp.valueOf(dateTime2));

            ResultSet results = statement.executeQuery();

            int n = 0;
            while (results.next()) {
                images.add(newImageMetadata(results));
                n++;
            }

            if (n == 0) throw new SQLException("ResultSet was empty");
        }
    }

    @Deprecated
    @VisibleForTesting
    public List<ImageMetadata> getPhotosUploadedBetween(LocalDateTime date1, LocalDateTime date2) throws SQLException {
        return getPhotosBetween(date1, date2, "uploadTimestamp");
    }

    public void deleteAll(String bucket) {
        String sql = "DELETE FROM Photo " +
                "WHERE bucketName = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bucket);
            statement.execute();
            Log.i(TAG, "DELETE ALL COMPLETED");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    @VisibleForTesting
    public List<ImageMetadata> getPhotosAround(double latitude,
                                               double latitudeDelta,
                                               double longitude,
                                               double longitudeDelta
    ) throws SQLException {

        String sql = "SELECT id, height, width, photoTimestamp, latitude, longitude, cameraSerialNumber, routeId, bearing, bearingAccuracy, locationAccuracy FROM Photo " +
                "WHERE latitude BETWEEN ? AND ? " +
                "AND longitude BETWEEN ? AND ?;";

        List<ImageMetadata> images = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, latitude - latitudeDelta);
            statement.setDouble(2, latitude + latitudeDelta);
            statement.setDouble(3, longitude - longitudeDelta);
            statement.setDouble(4, longitude + longitudeDelta);

            ResultSet results = statement.executeQuery();

            int n = 0;
            while (results.next()) {
                images.add(newImageMetadata(results));
                n++;
            }

            if (n == 0) throw new SQLException("ResultSet was empty");
        }

        return images;
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM Photo WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
            Log.i(TAG, "Row " + id + " was deleted from the database");
        }
    }

    //    ------------------------------------------------------------------------------------------------------------------

/*

    private void setSslProperties() throws IOException, CertificateException {
        System.setProperty("javax.net.ssl.trustStore", newKeyStoreFile().getPath());
        System.setProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE);
        System.setProperty("javax.net.ssl.trustStorePassword", DEFAULT_KEY_STORE_PASSWORD);
    }

    private File newKeyStoreFile() throws IOException, CertificateException {
        return newKeyStoreFile(newCertificate());
    }

    private File newKeyStoreFile(X509Certificate rootX509Certificate) throws IOException {

        File keyStoreFile = File.createTempFile(KEY_STORE_FILE_PREFIX, KEY_STORE_FILE_SUFFIX);

        try (FileOutputStream fos = new FileOutputStream(keyStoreFile.getPath())) {
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE, KEY_STORE_PROVIDER);
            ks.load(null);
            ks.setCertificateEntry("rootCaCertificate", rootX509Certificate);
            ks.store(fos, DEFAULT_KEY_STORE_PASSWORD.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | IOException e) {
            e.printStackTrace();
        }

        return keyStoreFile;
    }

    private X509Certificate newCertificate() throws CertificateException, MalformedURLException {

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//        URL url = new File(SSL_CERTIFICATE).toURI().toURL();
        URL url = DatabaseConnection.class.getClassLoader().getResource(SSL_CERTIFICATE);
        Objects.requireNonNull(url, "X509Certificate URL was null");


        try (InputStream certInputStream = url.openStream()) {
            return (X509Certificate) certFactory.generateCertificate(certInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
*/

}
