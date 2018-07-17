package client;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.google.common.annotations.VisibleForTesting;
import com.sun.org.apache.regexp.internal.RE;

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
import java.util.List;
import java.util.Objects;
import java.util.Properties;

class RdsConnection {

    private static final DefaultAWSCredentialsProviderChain creds = new DefaultAWSCredentialsProviderChain();
    private static final String AWS_ACCESS_KEY = creds.getCredentials().getAWSAccessKeyId();
    private static final String AWS_SECRET_KEY = creds.getCredentials().getAWSSecretKey();

    //    private static final Region REGION = Region.getRegion(Regions.EU_WEST_2);
    private static final Regions REGION = Regions.EU_WEST_2;
    private static final String HOSTNAME = "rds-mysql-uob-bristolstreetview.crvuxxvm3uvv.eu-west-2.rds.amazonaws.com";
    private static final int PORT = 3306;
    private static final String JDBC_URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/bristolstreetviewdb";
    private static final String USERNAME = "java-db-client";
    private static final String PASSWORD = "y2W06^*R^P4Xdtql"; // FIXME: 17/07/18 Password as plaintext!

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_PROVIDER = "SUN";
    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

    private static final String SSL_CERTIFICATE = "rds-ca-2015-eu-west-2.pem";

    private Connection connection;

    public RdsConnection() {

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


        System.out.println("HERE 2");
        Properties info = newMySqlProperties();
        Connection c = DriverManager.getConnection(JDBC_URL, info);
//        c.setAutoCommit(false);
        return c;
    }

    public void closeConnection() {
        try {
            this.connection.close();
            System.out.println("DB Connection closed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                       Date date,
                       Double latitude,
                       Double longitude,
                       String cameraSerialNumber,
                       int routeId) {

        String sql = "INSERT INTO Photo (id, height, width, date, latitude, longitude, cameraSerialNumber, routeId)" +
                "VALUES (" +
                id + "," +
                height + "," +
                width + "," +
                null + "," +
                latitude + "," +
                longitude + "," +
                cameraSerialNumber + "," +
                routeId + ")";

        return executeSqlUpdate(sql);
    }

    private int executeSqlUpdate(String sql) {
        int n = -1;

        try (Statement statement = connection.createStatement()) {
            n = statement.executeUpdate(sql);
            System.out.println("INSERT RESULT: " + n);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  n;
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

        System.out.println(">>>>>>>>>> HERE");

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//        URL url = new File(SSL_CERTIFICATE).toURI().toURL();
        URL url = RdsConnection.class.getClassLoader().getResource(SSL_CERTIFICATE);
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
