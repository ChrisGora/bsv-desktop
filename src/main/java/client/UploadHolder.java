package client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UploadHolder {

    private File file;
    private ImageMetadata metadata;
    private long totalSize;
    private long uploadedSize;
    private String key;
    private String bucket;

    private List<ProgressObserver> progressObservers;
    private List<CompletionObserver> uploadCompletionObservers;
    private List<CompletionObserver> dbUpdateCompletionObservers;
    private List<FailureObserver> uploadFailureObservers;
    private List<FailureObserver> dbFailureObservers;

    UploadHolder() {
        this.progressObservers = new ArrayList<>();
        this.uploadCompletionObservers = new ArrayList<>();
        this.dbUpdateCompletionObservers = new ArrayList<>();
        this.uploadFailureObservers = new ArrayList<>();
        this.dbFailureObservers = new ArrayList<>();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.totalSize = file.length();
    }

    public ImageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
    }

    public void onBytesUploaded(long bytesJustUploaded) {
        if (bytesJustUploaded != 0) {
            uploadedSize = uploadedSize + bytesJustUploaded;
            double progress = getProgress();
            for(ProgressObserver observer : progressObservers) {
                if (observer != null ) {
                    observer.onProgressChanged(progress);
                }
            }

            if (progress == 1) {
                for (CompletionObserver observer : uploadCompletionObservers) {
                    System.out.println("Completion observer called");
                    observer.onDone(this);
                }
            }
        }
    }

    public void onUploadFailure(String error) {
        for (FailureObserver observer : uploadFailureObservers) {
            observer.onFailure(error);
        }
    }

    public void onDbSuccess() {
        for (CompletionObserver observer : dbUpdateCompletionObservers) {
            observer.onDone(this);
        }
    }

    public void onDbFailure(String error) {
        for (FailureObserver observer : dbFailureObservers) {
            observer.onFailure(error);
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

    public void setUploadCompletionListener(CompletionObserver observer) {
        if (uploadCompletionObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else uploadCompletionObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    public void setUploadFailureListener(FailureObserver observer) {
        if (uploadFailureObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else uploadFailureObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    public void setDbUpdateCompletionListener(CompletionObserver observer) {
        if (dbUpdateCompletionObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else dbUpdateCompletionObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    public void setDbFailureListener(FailureObserver observer) {
        if (dbFailureObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else dbFailureObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }
}
