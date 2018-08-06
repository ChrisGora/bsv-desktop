package client;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class LocalStorageConnection implements StorageConnection {

    private FileHolder fileHolder;

    public LocalStorageConnection(FileHolder fileHolder) {
        this.fileHolder = fileHolder;
    }

    @Override
    public void copyFile() {
        Objects.requireNonNull(fileHolder.getFile(), "File was null");
        String source = fileHolder.getFile().getPath();
        String bucket  = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(fileHolder.getKey(), "Key was null");

        Path destinationPath = Paths.get(System.getProperty("user.home"), bucket, key);
        String destination = destinationPath.toString();

        File file = new File(destination);

        boolean createFileSuccessful = false;
        try {

            File parentFile = file.getParentFile();

            boolean mkdirSuccessful = parentFile.exists();
            if (!mkdirSuccessful) {
                mkdirSuccessful = parentFile.mkdirs();
            }

            if (mkdirSuccessful && !file.exists()) {
                createFileSuccessful = file.createNewFile();
            } else if (file.exists()){
                fileHolder.onUploadFailure("Duplicate filename");
                return;
            } else {
                fileHolder.onUploadFailure("Unknown error due to directory or empty file creation");
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
            fileHolder.onUploadFailure(e.toString());
        }

        System.out.println(">>>>>>>>>>> DESTINATION: " + destination);

        if (createFileSuccessful) {
            try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                    fileHolder.onBytesUploaded(length);
                }

            } catch (IOException e) {
                e.printStackTrace();
                fileHolder.onUploadFailure(e.toString());
            }

        }
    }

    @Override
    public void removeFile() {

        Objects.requireNonNull(fileHolder.getFile(), "File was null");
//        String source = fileHolder.getFile().getPath();
        String bucket  = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(fileHolder.getKey(), "Key was null");

        Path filePath = Paths.get(System.getProperty("user.home"), bucket, key);
        String filePathString = filePath.toString();

        File file = new File(filePathString);

        if (file.exists()) {
            boolean successful = file.delete();
            if (successful) fileHolder.onRemoveSuccess();
            else fileHolder.onRemoveFailure("Remove not successful");
        } else {
            fileHolder.onRemoveFailure("File does not exist");
        }
    }

    @Override
    public void removeAll() {
        String bucket  = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        Path filePath = Paths.get(System.getProperty("user.home"), bucket);
        String filePathString = filePath.toString();

        File folder = new File(filePathString);

        // FIXME: 06/08/18 Refactor to use the callbacks instead

        if ((!folder.isDirectory())) throw new AssertionError("File was not a directory");

        if (folder.exists()) {
            File[] files = folder.listFiles();
            Objects.requireNonNull(files, "List of files was null");
            for (File file : files) {
                if (file != null) file.delete();
            }
        } else throw new AssertionError("File does not exist");

    }
}
