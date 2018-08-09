package client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Includes all tests for the db client
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DatabaseConnectionTest.class,
        ImageMetadataTest.class,
        BucketHandlerTest.class
})

public class ClientTests {
}
