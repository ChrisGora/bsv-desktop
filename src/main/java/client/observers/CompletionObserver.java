package client.observers;

import client.FileHolder;

public interface CompletionObserver {

    void onDone(FileHolder fileHolder);

}
