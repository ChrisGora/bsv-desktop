package client;

import com.amazonaws.regions.Regions;

public class Uploader {
    private S3Connector s3Connector;
    private RdsConnector rdsConnector;

    Uploader() {
        this.s3Connector = new S3Connector(Regions.EU_WEST_2, "bristol-streetview-photos");
        this.rdsConnector = new RdsConnector();
    }

    public void test() {
        s3Connector.listBuckets();
    }

}
