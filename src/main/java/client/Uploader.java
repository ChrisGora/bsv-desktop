package client;

import com.adobe.xmp.XMPException;
import com.amazonaws.regions.Regions;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Uploader {
//    private S3Connection s3Connection;
    private RdsConnection rdsConnection;
    private String bucket = "bristol-streetview-photos";

    Uploader() {
//        this.s3Connection = new S3Connection(Regions.EU_WEST_2);
//        this.rdsConnection = new RdsConnection();
    }

//    public void test() {
//        s3Connection.listBuckets();
//    }

    public UploadHolder upload(File file) {
        String id = null;
        try {
            id = getImageId(file);
        } catch (ImageProcessingException | IOException | XMPException e) {
//            e.printStackTrace();
        }

        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }

        String key = id + "-" + file.getName();
        System.out.println(key);

        UploadHolder upload = new UploadHolder();
        upload.setFile(file);
        upload.setKey(key);
        upload.setBucket(bucket);

//        s3Connection.uploadFile(upload);

        S3Connection s3Connection = new S3Connection(Regions.EU_WEST_2, upload);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(s3Connection);
        return upload;
    }

    public void download() {

    }

    private String getImageId(File file) throws ImageProcessingException, IOException, XMPException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        String id = null;

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
//                System.out.println("readImageMetadata: " + directory.getName() + " " + tag.getTagName() + " " + tag.getDescription());
                if (tag.getTagType() == ExifDirectoryBase.TAG_IMAGE_UNIQUE_ID) {
                    System.out.println("HERE!!!!!");
                    id = tag.getDescription();
                    System.out.println(id);
                }
            }

            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.out.println("readImageMetadata: Metadata error: " + error);
                }
            }

//            if (directory.getName().equals("XMP")) {
//                System.out.println("readImageMetadata: XMP DETECTED");
//                XmpDirectory xmpDirectory = (XmpDirectory) directory;
//                XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
//                XMPIterator iterator = xmpMeta.iterator();
//                while (iterator.hasNext()) {
//                    XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
//                    Objects.requireNonNull(info);
//                    System.out.println("readImageMetadata: XMP: " + info.getPath() + " " + info.getValue());
//                }
//            }
        }

        return id;
    }

}