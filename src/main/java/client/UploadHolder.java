package client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UploadHolder {

    private File file;
    private long totalSize;
    private long uploadedSize;
    private String key;
    private String bucket;
    private List<ProgressObserver> progressObservers;
    private List<CompletionObserver> completionObservers;

    UploadHolder() {
        this.progressObservers = new ArrayList<>();
        this.completionObservers = new ArrayList<>();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.totalSize = file.length();
    }

    public void onBytesUploaded(long bytesJustUploaded) {
        uploadedSize = uploadedSize + bytesJustUploaded;
        double progress = getProgress();
        for(ProgressObserver observer : progressObservers) {
            if (observer != null ) {
                observer.onProgressChanged(progress);
            }
        }

        if (progress == 1) {
            for (CompletionObserver observer : completionObservers) {
                System.out.println("DONE!");
                observer.onDone();
            }
        }

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    private double getProgress() {
        double done = uploadedSize;
        double all = totalSize;
        return done/all;
    }

    public void setProgressListener(ProgressObserver observer) {
        if (progressObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else progressObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    public void setCompletionListener(CompletionObserver observer) {
        if (completionObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else completionObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }
}
