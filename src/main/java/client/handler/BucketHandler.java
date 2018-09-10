package client.handler;

import client.PhotoSet;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public interface BucketHandler extends AutoCloseable {

    FileHolder newFileHolder(File file);

    FileHolder newEmptyFileHolder();

    void upload(FileHolder upload);

    int saveJustUploadedAsNewRoute(int routeId);

    void deleteAll();

    PhotoSet getPhotosAround(double latitude, double longitude, int maxResults);
    void savePhotosAround(double latitude, double longitude, int maxResults) throws IOException;

    PhotoSet getPhotosTakenOn(LocalDateTime dateTime);
    void savePhotosTakenOn(LocalDateTime dateTime);

    PhotoSet getPhotosUploadedOn(LocalDateTime dateTime);
    void savePhotosUploadedOn(LocalDateTime dateTime);

    PhotoSet getPhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);
    void savePhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);

    PhotoSet getPhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);
    void savePhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);

}
