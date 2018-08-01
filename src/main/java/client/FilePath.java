package client;

import java.util.Objects;

public class FilePath {
    private String bucket;
    private String key;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = Objects.requireNonNull(bucket, "Bucket was null");
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = Objects.requireNonNull(key, "Key was null");
    }

    public String getPath() {
        return bucket + "/" + key;
    }
}
