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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(mixinStandardHelpOptions = true, version = "BSV DB CLIENT 1.0")
public class MainCLI2 implements Callable<Void> {

    @Option(names = {"-v", "--verbose"}, description = "Verbose output.")
    private boolean verbose = false;
    // TODO: 09/09/18 implement verbose

    @Option(names = {"-d", "--debug"}, description = "Debugging output.")
    private boolean debug = false;

    @Option(names = {"-u", "--upload"}, description = "Upload 360 degree images from the given uploadFolder.")
    private File uploadFolder;

    @Parameters(index = "0", paramLabel = "BUCKET", description = "Folder where the processed images are stored to process")
    private String bucket;

    private BucketHandler bucketHandler;
    private ProgressBar pb;
    private int numberOfImagesToProcess;
    private int done;


    public static void main(String[] args) {
        CommandLine.call(new MainCLI2(), args);
    }

    @Override
    public Void call() throws Exception {

        if (debug) Log.setDebugging();
        if (verbose) Log.setVerbose();

        bucketHandler = new ConcreteBucketHandler(bucket, StorageType.LOCAL);

        if (uploadFolder != null && uploadFolder.isDirectory()) {
            File[] files = uploadFolder.listFiles();
            Objects.requireNonNull(files, "File array was null");
            List<File> filesList = Arrays.asList(files);

            List<File> actualImages = filesList
                                            .stream()
                                            .filter((file) -> file.getPath().contains("_E.jpg"))
                                            .collect(Collectors.toList());

            setUpUploadProgressMonitoring(actualImages.size());

            actualImages.forEach(this::handleFile);
        }

        return null;
    }

    private void handleFile(File file) {
        if (file != null && file.getPath().contains("_E.jpg")) {
            FileHolder upload = bucketHandler.newFileHolder(file);
            if (upload != null) {
                setDefaultListeners(upload);
                bucketHandler.upload(upload);
            }
        }
    }

    private void setDefaultListeners(FileHolder upload) {
//        upload.setUploadCompletionListener(this::onDone);
        upload.setUploadFailureListener(System.err::println);
//
        upload.setDbUpdateCompletionListener(this::onDone);
        upload.setDbFailureListener(System.err::println);
//
        upload.setRemoveCompletionListener(System.out::println);
        upload.setRemoveFailureListener(System.err::println);
    }

    private void setUpUploadProgressMonitoring(int n) {
        pb = new ProgressBar("Processing...", n);
        numberOfImagesToProcess = n;
    }

    private synchronized void onDone(FileHolder fh) {
        pb.step();
        done++;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("DONE: " + fh.getKey());
        if (done == numberOfImagesToProcess) {
            try {
                bucketHandler.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pb.close();
            }
        }
    }
}
