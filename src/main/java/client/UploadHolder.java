package client;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.request.Progress;

import java.io.File;

public class UploadHolder {

    private File file;
    private ProgressEvent mostRecentProgressEvent;
    private String key;
    private String bucket;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ProgressEvent getMostRecentProgressEvent() {
        return mostRecentProgressEvent;
    }

    public void setMostRecentProgressEvent(ProgressEvent mostRecentProgressEvent) {
        this.mostRecentProgressEvent = mostRecentProgressEvent;
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

    public float getProgress() {
        return mostRecentProgressEvent.getBytesTransferred() / mostRecentProgressEvent.getBytes();
    }

}
