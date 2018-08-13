package client;

public class ProgressUpdateNotifier {
}
    /*
public class ProgressUpdateNotifier implements Runnable {

    private FileHolder fileHolder;
    private long bytesTransfered;

    public ProgressUpdateNotifier(FileHolder fileHolder, long bytesTransfered) {
        this.fileHolder = fileHolder;
        this.bytesTransfered = bytesTransfered;
    }

//    @Override
//    protected Integer call() throws Exception {
//        return 0;
//    }

    @Override
    public void run() {
        fileHolder.onBytesUploaded(bytesTransfered);
    }
}

    */