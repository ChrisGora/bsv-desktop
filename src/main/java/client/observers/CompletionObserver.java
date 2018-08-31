package client.observers;

import client.handler.FileHolder;

public interface CompletionObserver {

    void onDone(FileHolder fileHolder);

}
