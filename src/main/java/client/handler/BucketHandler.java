package client.handler;

import client.PhotoSet;
import client.observers.CompletionObserver;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public interface BucketHandler extends Closeable {

    FileHolder newFileHolder(File file);

    FileHolder newEmptyFileHolder();

    void upload(FileHolder upload);

    void upload(FileHolder upload, int routeNumber);

    int saveJustUploadedAsNewRoute(CompletionObserver callback, int routeId);

    PhotoSet getPhotosAround(double latitude, double longitude, int maxResults);
    void downloadPhotoSet(CompletionObserver callback, PhotoSet set) throws IOException;

    void downloadPhotos(CompletionObserver callback, String... ids);

    void deletePhotos(CompletionObserver callback, String... ids);
    void deleteAll(CompletionObserver callback);

//    PhotoSet getPhotosTakenOn(LocalDateTime dateTime);
//    void savePhotosTakenOn(LocalDateTime dateTime);
//
//    PhotoSet getPhotosUploadedOn(LocalDateTime dateTime);
//    void savePhotosUploadedOn(LocalDateTime dateTime);
//
//    PhotoSet getPhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);
//    void savePhotosTakenBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);
//
//    PhotoSet getPhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);
//    void savePhotosUploadedBetween(LocalDateTime dateTime1, LocalDateTime dateTime2);

}
