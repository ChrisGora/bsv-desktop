package client;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class ImageMetadata {

    private File file;
    private String id;
    private int height;
    private int width;
    private Date date;
    private double latitude;
    private double longitude;
    private String serialNumber;


    public ImageMetadata(File file) throws ImageProcessingException, IOException, MetadataException {
        this.file = file;
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        this.id = exifSubIFDDirectory.getda
        this.height= jpegDirectory.getImageHeight();
        this.width = jpegDirectory.getImageWidth();

    }

/*    public ImageMetadata(File file) throws ImageProcessingException, IOException {

        this.file = file;

        Metadata metadata = ImageMetadataReader.readMetadata(file);

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {

                int type = tag.getTagType();

                switch (type) {
                    case ExifDirectoryBase.TAG_IMAGE_UNIQUE_ID: {
                        id = tag.getDescription();
                        System.out.println(id);
                        break;
                    }
                    case ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH: {
                        width = Integer.parseInt(tag.getDescription().replace(" pixels", ""));
                        System.out.println(width);
                        break;
                    }
                    case ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT: {
                        height = Integer.parseInt(tag.getDescription().replace(" pixels", ""));
                        System.out.println(height);
                        break;
                    }
//                    case ExifDirectoryBase.TAG_DATETIME:
                    // FIXME: 17/07/18 Replace the date/time field created by Ricoh with the date/time from the tablet!

                    case ExifDirectoryBase.

                }

//                if (tag.getTagType() == ExifDirectoryBase.TAG_IMAGE_UNIQUE_ID) id = tag.getDescription();

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
    }*/

    public void printMetadata() throws ImageProcessingException, IOException, XMPException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.println("readImageMetadata: " + directory.getName() + " " + tag.getTagName() + " " + tag.getDescription());
            }

            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.out.println("readImageMetadata: Metadata error: " + error);
                }
            }

            if (directory.getName().equals("XMP")) {
                System.out.println("readImageMetadata: XMP DETECTED");
                XmpDirectory xmpDirectory = (XmpDirectory) directory;
                XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
                XMPIterator iterator = xmpMeta.iterator();
                while (iterator.hasNext()) {
                    XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
                    Objects.requireNonNull(info);
                    System.out.println("readImageMetadata: XMP: " + info.getPath() + " " + info.getValue());
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Date getDate() {
        return date;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
