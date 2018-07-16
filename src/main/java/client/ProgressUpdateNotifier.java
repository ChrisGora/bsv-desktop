package client;

import javafx.concurrent.Task;

public class ProgressUpdateNotifier implements Runnable {

    private UploadHolder uploadHolder;
    private long bytesTransfered;

    public ProgressUpdateNotifier(UploadHolder uploadHolder, long bytesTransfered) {
        this.uploadHolder = uploadHolder;
        this.bytesTransfered = bytesTransfered;
    }

//    @Override
//    protected Integer call() throws Exception {
//        return 0;
//    }

    @Override
    public void run() {
        uploadHolder.onBytesUploaded(bytesTransfered);
    }
}
