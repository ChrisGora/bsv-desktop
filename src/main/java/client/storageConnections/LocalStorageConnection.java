package client.storageConnections;

import client.handler.FileHolder;
import client.util.Log;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores the photos in a local folder in the user's home directory:
 * /HOME/BUCKET_NAME
 *
 * @author Chris Gora
 * @version 1.0, 01.09.2018
 */
public class LocalStorageConnection extends StorageConnection {

    private static final String TAG = "LocalStorageConnection";

    public LocalStorageConnection(FileHolder fileHolder) {
        super(fileHolder, StorageType.LOCAL);
    }

    @Nullable
    @Override
    public File getFile(String key) throws IOException {
        return new File(getDestination(false, key));
    }

    @Override
    public void copyFile() {
        String source = getSource();
        String destination = null;
        try {
            destination = getDestination(false);
        } catch (IOException e) {
            fileHolder.onUploadFailure(e.toString());
            return;
        }

        File file = new File(destination);
        copy(source, destination, file);
    }

    @Override
    public void copyFileToOutput() {
        String source = getSource();
        String destination = null;
        try {
            destination = getDestination(true);
        } catch (IOException e) {
            fileHolder.onUploadFailure(e.toString());
            return;
        }

        if (source.contains(".json") && Log.debugging){

            try {
                BufferedReader br = new BufferedReader(new FileReader(fileHolder.getFile()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println("<<<<");
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        File file = new File(destination);
        copy(source, destination, file);
    }

    private String getDestination(boolean output) throws IOException {
        return getDestination(output, fileHolder.getKey());
    }

    private String getDestination(boolean output, String key) throws IOException {
        String bucket = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        Objects.requireNonNull(key, "Key was null");
        Path destinationPath = null;
        if (output) {
            destinationPath = Paths.get(System.getProperty("user.home"), bucket, "output", key);
            FileUtils.deleteDirectory(new File(destinationPath.toString()));
        }
        else destinationPath = Paths.get(System.getProperty("user.home"), bucket, key);
        return destinationPath.toString();
    }

    private String getSource() {
        Objects.requireNonNull(fileHolder.getFile(), "File was null");
        return fileHolder.getFile().getPath();
    }

    private void copy(String source, String destination, File file) {
        boolean createFileSuccessful = false;
        try {

            File parentFile = file.getParentFile();

            boolean mkdirSuccessful = parentFile.exists();
            if (!mkdirSuccessful) {
                mkdirSuccessful = parentFile.mkdirs();
            }

            if (mkdirSuccessful && !file.exists()) {
                createFileSuccessful = file.createNewFile();
            } else if (file.exists()) {
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

        Log.v(TAG, "Ready to start uploading... FILE DESTINATION: " + destination);

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
        String filePathString = null;
        try {
            filePathString = getDestination(false);
        } catch (IOException e) {
            fileHolder.onRemoveFailure(e.toString());
        }

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
    public void removeAll() throws IOException {
        String bucket = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        Path filePath = Paths.get(System.getProperty("user.home"), bucket);
        String filePathString = filePath.toString();

        File folder = new File(filePathString);

        // FIXME: 06/08/18 Refactor to use the callbacks instead

        if (!folder.exists()) {
            fileHolder.onRemoveFailure("Folder does not exist");
        }
        else if ((!folder.isDirectory())) {
            fileHolder.onRemoveFailure("File was not a directory");
        }
        else if (folder.exists()) {
            FileUtils.deleteDirectory(folder);
            fileHolder.onRemoveSuccess();
        } else {
            fileHolder.onRemoveFailure("Remove failure - unknown reason");
        }

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<RTree<String, Geometry>> getRTree() {
        File rTree = getRTreeFile();
        try {
            if (rTree != null && rTree.exists() && rTree.length() != 0) {
                RTree<String, Geometry> tree = Serializers.flatBuffers().utf8().read(new FileInputStream(rTree), rTree.length(), InternalStructure.DEFAULT);
                return Optional.of(tree);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void saveRTree(RTree<String, Geometry> tree) throws IOException {
        File rTree = Objects.requireNonNull(getRTreeFile());
        String bucket = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(RTREE_FILE, "Key was null");

        // TODO: 09/09/18 test me

        Log.d(TAG, "saveRTree: Trying to save RTREE");
        try (OutputStream out = new FileOutputStream(rTree)) {

            Serializer<String, Geometry> serializer = Serializers.flatBuffers().utf8();
            serializer.write(tree, out);

            Log.i(TAG, "RTREE Saved");

        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            Log.w(TAG, "saveRTree: BUCKET NOT FOUND");
        }

    }

    private File getRTreeFile() {
        String bucket = Objects.requireNonNull(fileHolder.getBucket(), "Bucket was null");
        String key = Objects.requireNonNull(RTREE_FILE, "Key was null");

        Path filePath = Paths.get(System.getProperty("user.home"), bucket, key);
        String filePathString = filePath.toString();

        File file = new File(filePathString);
//        new File(file.getParentFile().toURI()).mkdirs();
        return file;
    }
}
