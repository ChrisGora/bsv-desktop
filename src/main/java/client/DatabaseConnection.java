package client;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

class DatabaseConnection implements AutoCloseable {

    private static final DefaultAWSCredentialsProviderChain creds = new DefaultAWSCredentialsProviderChain();
    private static final String AWS_ACCESS_KEY = creds.getCredentials().getAWSAccessKeyId();
    private static final String AWS_SECRET_KEY = creds.getCredentials().getAWSSecretKey();

    //    private static final Region REGION = Region.getRegion(Regions.EU_WEST_2);
    private static final Regions REGION = Regions.EU_WEST_2;
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 3306;
    private static final String JDBC_URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/bristol_streetview_schema";
    private static final String USERNAME = "java-db-client";
    private static final String PASSWORD = "v1M4^qVAU!3084NF"; // FIXME: 17/07/18 Password as plaintext!

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_PROVIDER = "SUN";
    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

    private static final String SSL_CERTIFICATE = "rds-ca-2015-eu-west-2.pem";

    private Connection connection;

    public DatabaseConnection() {

        try {
            this.connection = newConnection();
        } catch (SQLException | IOException | CertificateException e) {
            e.printStackTrace();
        }

        testConnection();
//        closeConnection();
    }


    private void testConnection() {
        String sql = "SELECT 'Success!' FROM DUAL;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                String output = set.getString(1);
                System.out.println("DB says: " + output);
                if (!output.equals("Success!")) {
                    throw new SQLWarning("Database Success Message not received");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection newConnection() throws SQLException, IOException, CertificateException {
        setSslProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


//        System.out.println("HERE 2");
        Properties info = newMySqlProperties();
        Connection c = DriverManager.getConnection(JDBC_URL, info);
//        c.setAutoCommit(false);
        return c;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
        System.out.println("DB Connection closed");
    }

    private Properties newMySqlProperties() {
        Properties mysqlProperties = new Properties();

        mysqlProperties.setProperty("verifyServerCertificate","true");
        mysqlProperties.setProperty("useSSL", "false");
        mysqlProperties.setProperty("user", USERNAME);
        mysqlProperties.setProperty("password", PASSWORD);

        return mysqlProperties;
    }

    public int insertPhotoRow(String id,
                       int height,
                       int width,
                       LocalDateTime photoDateTime,
                       LocalDateTime uploadDateTime,
                       Double latitude,
                       Double longitude,
                       String cameraSerialNumber,
                       int routeId,
                       String bucketName,
                       String key
    ) throws SQLException {

        String sql = "INSERT INTO Photo " +
                "(id, height, width, photoTimestamp, uploadTimestamp, latitude, longitude, cameraSerialNumber, routeId, bucketName, fileKey) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?)";

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

            n = statement.executeUpdate();

//        } catch (SQLException e) {
//            e.printStackTrace();
        }

        System.out.println("INSERT RESULT: " + n);
        return  n;

    }

    public FilePath getPath(String id) throws SQLException {
        String sql = "SELECT bucketName, fileKey FROM Photo " +
                "WHERE (id = ?);";

        String bucket = null;
        String key = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();

            int n = results.getFetchSize();
            System.out.println("FETCH SIZE: " + n);

//            while (results.next()) {
                results.first();
                bucket = results.getString("bucketName");
                key = results.getString("fileKey");
                System.out.println(bucket);
                System.out.println(key);
//            }
        }

        if (bucket == null || key == null) {
            throw new SQLException("Bucket or key was null");
        } else {
            FilePath filePath = new FilePath();
            filePath.setBucket(bucket);
            filePath.setKey(key);
            return filePath;
        }

    }

    public List<String> getPhotosWithExactMatch(double latitude, double longitude) throws SQLException {
        String sql =    "SELECT id FROM Photo " +
                        "WHERE latitude = ? " +
                        "AND longitude = ?;";

        List<String> photoIds = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, latitude);
            statement.setDouble(2, longitude);

            ResultSet results = statement.executeQuery();

            int n = 0;
            while (results.next()) {
                photoIds.add(results.getString(1));
                n++;
            }

            if (n == 0) {
                throw new SQLWarning("ResultSet was empty");
            }
        }
        return photoIds;
    }
//
//    Time of photo being taken:
//    public List<String> getPhotosWithExactMatch(LocalDateTime dateTime) {
//
//    }
//
//    Time of photo uploaded:
//    public List<String> getPhotosWithExactMatch(LocalDateTime dateTime) {
//
//    }
//
//    public List<String> getPhotosClosestTo(double longitude, double latitude) {
//
//    }
//


    public void deleteAll() {
        String sql = "TRUNCATE TABLE Photo;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
            System.out.println("DELETE ALL: Done");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

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

        System.out.println(url);

        try (InputStream certInputStream = url.openStream()) {
            return (X509Certificate) certFactory.generateCertificate(certInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
