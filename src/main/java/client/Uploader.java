package client;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.amazonaws.regions.Regions;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class Uploader {
    private S3Connector s3Connector;
    private RdsConnector rdsConnector;
    private String bucket = "bristol-streetview-photos";

    Uploader() {
        this.s3Connector = new S3Connector(Regions.EU_WEST_2);
        this.rdsConnector = new RdsConnector();
    }

    public void test() {
        s3Connector.listBuckets();
    }

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
        s3Connector.uploadFile(upload);

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