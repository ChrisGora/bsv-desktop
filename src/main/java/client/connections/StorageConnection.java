package client.connections;

import client.FileHolder;
import client.StorageType;

/**
 * An interface for interacting with a photo file storage.
 *
 * Each implementation has a corresponding {@link StorageType} enum.
 * File can only accepted in the form of a {@link FileHolder}.
 * One instance of a Storage Connection should only be responsible for uploading one file.
 *
 * @author Chris Gora
 * @version 1.0, 01.09.2018
 */
public abstract class StorageConnection {

    final FileHolder fileHolder;
    private final StorageType type;

    StorageConnection(FileHolder fileHolder, StorageType type) {
        this.fileHolder = fileHolder;
        this.type = type;
    }

    /**
     * Returns the {@link StorageType} enum associated with this Connection.
     */
    public StorageType getType() {
        return type;
    }

    /**
     * Returns the {@link FileHolder} associated with this connection.
     * Only one FileHolder can be associated with one connection.
     */
    public FileHolder getFileHolder() {
        return fileHolder;
    }

    /**
     * Uploads / copies the file into the specified bucket.
     * Calls the following callback methods:
     *      <li> {@link FileHolder#onBytesUploaded(long)}
     *      <li> {@link FileHolder#onUploadFailure(String)}
     *
     * Should be submitted as a Runnable and not executed on the main thread.
     */
    public abstract void copyFile();

    /**
     * Removes a file from the bucket.
     * Calls the following callback methods:
     *      <li> {@link FileHolder#onRemoveSuccess()}
     *      <li> {@link FileHolder#onRemoveFailure(String)}
     *
     * Should be submitted as a Runnable and not executed on the main thread.
     */
    public abstract void removeFile();

    /**
     * Removes all files from a bucket (and that bucket only).
     * Calls the following callback methods:
     *      <li> {@link FileHolder#onRemoveSuccess()}
     *      <li> {@link FileHolder#onRemoveFailure(String)}
     *
     * Should be submitted as a Runnable and not executed on the main thread.
     */
    public abstract void removeAll();
    // FIXME: 10/08/18 Callbacks are not in use here yet
    // FIXME: 10/08/18 Implement remove all in S3

    // TODO: 10/08/18 Decide on how a downloaded file should be returned
//    File getFile();
}