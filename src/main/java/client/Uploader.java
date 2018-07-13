package client;

import com.amazonaws.regions.Regions;

import java.io.File;

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

    public void upload(File file) {
        String key = file.getName();
        System.out.println(key);
//        s3Connector.uploadFile(key, file);
    }

    public void download() {

    }
}
