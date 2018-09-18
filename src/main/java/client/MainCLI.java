package client;

import client.handler.BucketHandler;
import client.handler.ConcreteBucketHandler;
import client.handler.FileHolder;
import client.storageConnections.StorageType;
import client.util.Log;
import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(mixinStandardHelpOptions = true, version = "BSV DB CLIENT 1.0")
public class MainCLI implements Callable<Void> {

    private static final String TAG = "MAIN";



    @Option(names = {"-v", "--verbose"}, description = "Verbose output.")
    private boolean verbose = false;

    @Option(names = {"-e", "--debug"}, description = "Debugging output.")
    private boolean debug = false;

    @Option(names = {"-p", "--noPrintOuts"}, description = "No printed output. Just the progress bar.")
    private boolean noPrintOuts = false;

    @Option(names = {"-f", "--logToFile"}, description = "Send the log output to the specified file.")
    private String logToFile;

    @Option(names = {"-u", "--upload"}, description = "Upload 360 degree images from the given folder.")
    private File folderToUpload;

    @Option(names = {"-g", "--saveAsGpxAfterUpload"}, description = "Collect all just uploaded files into a GPX file")
    private boolean saveAsGpxAfterUpload;

    // TODO: 13/09/18 Implement me
//    @Option(names = {"-x", "--saveRouteAsGpx"}, description = "Collect all entries with the specified route ID into a GPX file. A route ID must be specified with '-r'.")
//    private boolean saveRouteAsGpx;

    @Option(names = {"-s", "--save"}, description = "Save photos with the corresponding ids to the output directory.")
    private boolean save;

    @Option(names = {"-d", "--delete"}, description = "Delete the photos with the corresponding ids.")
    private boolean delete;

    @Option(names = {"--deleteAll"}, description = "Delete all elements from the bucket.")
    private boolean deleteAll;

    @Option(names = {"-r", "--route"}, description = "Route ID that will be associated with the uploaded pictures.")
    private int route;

    @Option(names = {"--geo"}, description = "Conduct a geographic search within the given range (in meters).")
    private double geographicSearchRadius;

    @Option(names = {"--latitude"}, description = "Set latitude for the geographic search")
    private double latitude;

    @Option(names = {"--longitude"}, description = "Set longitude for the geographic search")
    private double longitude;

    @Option(names = {"--maxGeoResults"}, description = "Maximum number of results allowed from the geographical search. Defaults to 100")
    private int maxGeoResults = 100;

    @Option(names = {"-b", "--bucket"}, description = "Folder where the processed images are or will be stored.")
    private String bucket;

    @Parameters(index = "0..*", paramLabel = "IDS", description = "IDs of files to process (what will be done depends on selected options).")
    private String[] ids;

    private BucketHandler bucketHandler;
    private ProgressBar pb;
    private int numberOfImagesToProcess;
    private int done;


    public static void main(String[] args) {
        CommandLine.call(new MainCLI(), args);
    }

    @Override
    public Void call() throws IOException {

        if (debug) Log.setDebugging();
        if (verbose) Log.setVerbose();
        if (noPrintOuts) Log.disable();
        if (logToFile != null) {
            try {
                String path = new File(logToFile).getAbsolutePath();
                System.out.println(path);
                Log.logToFile(path);
            } catch (IOException e) {
                Log.stopLoggingToFile();
            }
        }

        if (folderToUpload != null && folderToUpload.isDirectory()) {
            System.out.println("UPLOADING FROM " + folderToUpload.getAbsolutePath());
            bucketHandler = getBucketHandler();
            File[] files = folderToUpload.listFiles();
            Objects.requireNonNull(files, "File array was null");
            List<File> filesList = Arrays.asList(files);

            List<File> actualImages = filesList
                    .stream()
                    .filter((file) -> file.getPath().contains("_E.jpg"))
                    .collect(Collectors.toList());

            if (saveAsGpxAfterUpload) {
                setProgressMonitoring(actualImages.size() + 1);
            } else {
                setProgressMonitoring(actualImages.size());
            }
            actualImages.forEach(this::handleFile);
        } else if (save) {
            System.out.println("RETRIEVING PHOTOS...");
            bucketHandler = getBucketHandler();
            setProgressMonitoring(ids.length);
            bucketHandler.downloadPhotos(this::onDone, ids);
        } else if (delete) {
            System.out.println("DELETING PHOTOS...");
            bucketHandler = getBucketHandler();
            setProgressMonitoring(ids.length);
            bucketHandler.deletePhotos(this::onDone, ids);
        } else if (deleteAll) {
            System.out.println("DELETING ALL PHOTOS...");
            bucketHandler = getBucketHandler();
            setProgressMonitoring(1);
            bucketHandler.deleteAll(this::onDone);
        } else if (geographicSearchRadius != 0) {
            System.out.println("GEOGRAPHIC SEARCH...");
            bucketHandler = new ConcreteBucketHandler(bucket, StorageType.LOCAL, geographicSearchRadius);
            PhotoSet set = bucketHandler.getPhotosAround(latitude, longitude, maxGeoResults);
            setProgressMonitoring(set.getIds().size());
            bucketHandler.downloadPhotoSet(this::onDone, set);
        }
        System.out.println("FINISHED!");
        return null;
    }

    private ConcreteBucketHandler getBucketHandler() {
        return new ConcreteBucketHandler(bucket, StorageType.LOCAL);
    }

    private void handleFile(File file) {
        if (file != null && file.getPath().contains("_E.jpg")) {
            FileHolder upload = bucketHandler.newFileHolder(file);
            if (upload != null) {
                setDefaultListeners(upload);
                bucketHandler.upload(upload, route);
            }
        }
    }

    private void setDefaultListeners(FileHolder upload) {
//        upload.setUploadCompletionListener(this::onDone);
        upload.setUploadFailureListener((error) -> Log.e(TAG, error));
//
        upload.setDbUpdateCompletionListener(this::onDone);
        upload.setDbFailureListener((error) -> Log.e(TAG, error));
//
        upload.setRemoveCompletionListener((error) -> Log.w(TAG, error.getKey()));
        upload.setRemoveFailureListener((error) -> Log.e(TAG, error));
    }

    private void setProgressMonitoring(int n) {
        pb = new ProgressBar("Processing...", n);
        numberOfImagesToProcess = n;
    }

    private synchronized void onDone(FileHolder fh) {
        pb.step();
        done++;
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        Log.i(TAG, done + " DONE: " + fh.getKey());

        if ((done == numberOfImagesToProcess - 1) && saveAsGpxAfterUpload) {
            bucketHandler.saveJustUploadedAsNewRoute(this::onDone, route);
        }

        if (done == numberOfImagesToProcess) {
            try {
                bucketHandler.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pb.close();
                Log.close();
            }
        }
    }
}
