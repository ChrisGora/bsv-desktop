package client;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;

import java.util.List;
import java.util.Objects;

class RdsConnection {

    private static final Regions REGION = Regions.EU_WEST_2;
    DBInstance db;

    public RdsConnection() {

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
    }
}
