package client;

import client.databaseConnections.DatabaseConnectionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Includes all tests for the db client
 *
 * @author Chris Gora
 * @version 1.0, 01.09.2018
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DatabaseConnectionTest.class,
        ImageMetadataTest.class,
        BucketHandlerTest.class
})

public class ClientTests {}
