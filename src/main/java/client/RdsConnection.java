package client;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.google.common.annotations.VisibleForTesting;
import org.apache.http.client.entity.UrlEncodedFormEntity;

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

class RdsConnection {

//    private static final DefaultAWSCredentialsProviderChain creds = new DefaultAWSCredentialsProviderChain();
//    private static final String AWS_ACCESS_KEY = creds.getCredentials().getAWSAccessKeyId();
//    private static final String AWS_SECRET_KEY = creds.getCredentials().getAWSSecretKey();

//    private static final Region REGION = Region.getRegion(Regions.EU_WEST_2);
    private static final Regions REGION = Regions.EU_WEST_2;
//    private static final String HOSTNAME = "rds-mysql-uob-bristolstreetview.crvuxxvm3uvv.eu-west-2.rds.amazonaws.com";
//    private static final int PORT = 3306;
//    private static final String JDBC_URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT;
//    private static final String USERNAME = "kg17815";
//
//    private static final String KEY_STORE_TYPE = "JKS";
//    private static final String KEY_STORE_PROVIDER = "SUN";
//    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
//    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
//    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";
//
//    private static final String SSL_CERTIFICATE = "rds-ca-2015-eu-west-2.pem";

//    private String authToken;
//    private Connection connection;

    DBInstance db;

    public RdsConnection() {
//        System.out.println(AWS_ACCESS_KEY);
//        System.out.println(AWS_SECRET_KEY);

//        this.authToken = newAuthToken();
//        System.out.println(authToken);

        AmazonRDS rds = AmazonRDSClientBuilder
                .standard()
                .withRegion(REGION)
                .build();

        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest();
        DescribeDBInstancesResult result = rds.describeDBInstances(request);
        List<DBInstance> list = result.getDBInstances();
        System.out.println("list length = " + list);

        for (DBInstance db : list) {
            if (db.getDBInstanceIdentifier().equals("rds-mysql-uob-bristolstreetview")) {
                System.out.println("YASSSSS");
                this.db = db;
                break;
            }
        }

        Objects.requireNonNull(this.db, "Database not found");

//        try {
//            this.connection = newConnection();
//        } catch (SQLException | CertificateException | IOException e) {
//            e.printStackTrace();
//        }
    }

//    @VisibleForTesting
//    String getAuthToken() {
//        return authToken;
//    }

/*    private String newAuthToken() {
        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator
                .builder()
                .credentials(new DefaultAWSCredentialsProviderChain())
                .region(Region.getRegion(REGION))
                .build();
        String authToken = generator.getAuthToken(
                GetIamAuthTokenRequest
                        .builder()
                        .hostname(HOSTNAME)
                        .port(PORT)
                        .userName(USERNAME)
                        .build()
        );

        return authToken;
    }*/

/*    private Connection newConnection() throws SQLException, IOException, CertificateException {
//        setSslProperties();
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        return DriverManager.getConnection(JDBC_URL, newMySqlProperties());
    }

    private Properties newMySqlProperties() {
        Properties mysqlProperties = new Properties();

        mysqlProperties.setProperty("verifyServerCertificate","true");
        mysqlProperties.setProperty("useSSL", "false");
        mysqlProperties.setProperty("user", USERNAME);
        mysqlProperties.setProperty("password", authToken);

        return mysqlProperties;
    }

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
        URL url = RdsConnection.class.getClassLoader().getResource(SSL_CERTIFICATE);
        Objects.requireNonNull(url, "X509Certificate URL was null");
        try (InputStream certInputStream = url.openStream()) {
            return (X509Certificate) certFactory.generateCertificate(certInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

}
