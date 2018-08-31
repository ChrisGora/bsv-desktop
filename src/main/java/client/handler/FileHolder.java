package client.handler;

import client.databaseConnections.ImageMetadata;
import client.observers.CompletionObserver;
import client.observers.FailureObserver;
import client.observers.ProgressObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileHolder {

    private File file;
    private ImageMetadata metadata;
    private long totalSize;
    private long uploadedSize;
    private String key;
    private String bucket;

    private final List<ProgressObserver> progressObservers;
    private final List<CompletionObserver> uploadCompletionObservers;
    private final List<FailureObserver> uploadFailureObservers;

    private final List<CompletionObserver> dbUpdateCompletionObservers;
    private final List<FailureObserver> dbFailureObservers;

    private final List<CompletionObserver> removeCompletionObservers;
    private final List<FailureObserver> removeFailureObservers;

    FileHolder() {
        this.progressObservers = new ArrayList<>();
        this.uploadCompletionObservers = new ArrayList<>();
        this.dbUpdateCompletionObservers = new ArrayList<>();
        this.uploadFailureObservers = new ArrayList<>();
        this.dbFailureObservers = new ArrayList<>();
        this.removeCompletionObservers = new ArrayList<>();
        this.removeFailureObservers = new ArrayList<>();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.totalSize = file.length();
    }

    ImageMetadata getMetadata() {
        return metadata;
    }

    void setMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
    }

    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public String getBucket() {
        return bucket;
    }

    void setBucket(String bucket) {
        this.bucket = bucket;
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

    public void onRemoveSuccess() {
        for (CompletionObserver observer : removeCompletionObservers) {
            observer.onDone(this);
        }
    }

    public void onRemoveFailure(String error) {
        for (FailureObserver observer : removeFailureObservers) {
            observer.onFailure(error);
        }
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

    public void setRemoveCompletionListener(CompletionObserver observer) {
        if (removeCompletionObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else removeCompletionObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    public void setRemoveFailureListener(FailureObserver observer) {
        if (removeFailureObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else removeFailureObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }
}
