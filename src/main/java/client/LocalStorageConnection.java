package client;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorageConnection implements Runnable {

    private UploadHolder uploadHolder;

    public LocalStorageConnection(UploadHolder uploadHolder) {
        this.uploadHolder = uploadHolder;
    }

//    @Override
//    protected Void call() throws Exception {
//        copyFile();
//        return null;
//    }


    @Override
    public void run() {
        copyFile();
    }

    private void copyFile() {
        String source = uploadHolder.getFile().getPath();
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Path destinationPath = Paths.get(System.getProperty("user.home"), "bsv_photos", uploadHolder.getKey());
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
                uploadHolder.onUploadFailure("Duplicate filename");
                return;
            } else {
                uploadHolder.onUploadFailure("Unknown error due to directory or empty file creation");
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
            uploadHolder.onUploadFailure(e.toString());
        }

        System.out.println(">>>>>>>>>>> DESTINATION: " + destination);

        if (createFileSuccessful) {
            try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                    uploadHolder.onBytesUploaded(length);
                }

            } catch (IOException e) {
                e.printStackTrace();
                uploadHolder.onUploadFailure(e.toString());
            }

        }


    }
}
