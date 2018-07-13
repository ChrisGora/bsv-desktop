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
    private List<ProgressObserver> observers;

    UploadHolder() {
        this.observers = new ArrayList<>();
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
        for(ProgressObserver observer : observers) {
            observer.onProgressChanged(getProgress());
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

    public double getProgress() {
        double done = uploadedSize;
//        System.out.println("DONE: " + done);
        double all = totalSize;
//        System.out.println("ALL: " + all);
        double progress = done/all;
        System.out.println("PROGRESS: " + progress);
        return progress;
    }

    public void setProgressListener(ProgressObserver observer) {
        if (observers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else observers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }
}
