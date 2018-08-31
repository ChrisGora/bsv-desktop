package client;

import client.storageConnections.StorageType;
import client.handler.BucketHandler;
import client.handler.FileHolder;

import java.io.File;
import java.util.Objects;

public class MainCLI {



    private static final String HELP_NAME = "Help";
    private static final String HELP_COMMAND = "-h";

    private static final String UPLOAD_360_NAME = "Upload 360 photos";
    private static final String UPLOAD_360_COMMAND = "-u360";

    private static final String UPLOAD_SINGLE_NAME = "Upload a single photo";
    private static final String UPLOAD_SINGLE_COMMAND = "-us";

//    private static final String UPLOAD_MULTIPLE_NAME = "Upload multiple photos";
//    private static final String UPLOAD_MULTIPLE_COMMAND = "-um";

    private static final String SAVE_ROUTE_NAME = "Save route";
    private static final String SAVE_ROUTE_COMMAND = "-r";



    public static void main(String[] args) {
        int n = args.length;
        if (n <= 2) {
            printHelp();
        } else {
            String bucketName = args[1];
            String bucketType = args[2];

            if (bucketName != null && bucketType != null) {
                int i = 3;
                while (i < n) {
                    switch (args[n]) {
                        case UPLOAD_360_COMMAND:            upload360(n, args, bucketName, bucketType);
                                                        break;
                        case UPLOAD_SINGLE_COMMAND:         uploadSingle(n, args, bucketName, bucketType);
                                                        break;
                        case SAVE_ROUTE_COMMAND:        saveRoute(n, args, bucketName, bucketType);
                                                        break;
                    }
                }
            }
        }
    }


    private static void printHelp() {
        System.out.println("First argument must be the bucket name");
        System.out.println("Second argument must be the storage type: 'S3' OR 'LOCAL'");
        printHelpSingleLine(HELP_NAME, HELP_COMMAND);
        printHelpSingleLine(UPLOAD_360_NAME, UPLOAD_360_COMMAND);
        printHelpSingleLine(UPLOAD_SINGLE_NAME, UPLOAD_SINGLE_COMMAND);
        printHelpSingleLine(SAVE_ROUTE_NAME, SAVE_ROUTE_COMMAND);
    }

    private static void printHelpSingleLine(String name, String command) {
      System.out.printf("%-30s %s\n", name, command);
    }


//    n is the index of the command
    private static void upload360(int n, String[] args, String bucketName, String bucketType) {
        String path = args[n + 1];
        File directory = new File(path);
        File[] files = directory.listFiles();

        BucketHandler bucketHandler;
        if ("S3".equals(bucketType)) bucketHandler = new BucketHandler(bucketName, StorageType.AMAZON);
        else if ("LOCAL".equals(bucketType)) bucketHandler = new BucketHandler(bucketName, StorageType.LOCAL);
        else throw new IllegalArgumentException("Incorrect bucket type");

        for (File file : Objects.requireNonNull(files)) {
            if ((file != null) && file.getPath().contains("_E.jpg")) {
                FileHolder upload = bucketHandler.newFileHolder(file);
                bucketHandler.upload(upload);
                // TODO: 20/08/18 Set listeners and maybe do synchronized? Check if the app exits before work is done!!
            }
        }
    }

    private static void uploadSingle(int n, String[] args, String bucketName, String bucketType) {

    }


    private static void saveRoute(int n, String[] args, String bucketName, String bucketType) {

    }

}
