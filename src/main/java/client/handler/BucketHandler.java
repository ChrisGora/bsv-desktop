package client.handler;

import client.PhotoSet;

import javax.annotation.Nullable;
import java.io.File;

public interface BucketHandler extends AutoCloseable {

    FileHolder newFileHolder(File file);

    FileHolder newEmptyFileHolder();

    void upload(FileHolder upload);

    int saveJustUploadedAsNewRoute(int routeId);

    void deleteAll();

    @Nullable
    PhotoSet getPhotos(double latitude, double longitude);
}
