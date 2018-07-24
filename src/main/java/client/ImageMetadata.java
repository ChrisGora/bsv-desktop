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
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageData;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.*;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ImageMetadata {

    private File file;
    private String id;
    private int height;
    private int width;
    private LocalDateTime photoDateTime;
    private double latitude;
    private double longitude;
    private String serialNumber;




    public ImageMetadata(File file) throws IOException, MetadataException, ImageProcessingException, ImageReadException {
        this.file = file;
        readJpegMetadata();
        readExifMetadata();
    }

    private void readJpegMetadata() throws ImageProcessingException, IOException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        this.height= jpegDirectory.getImageHeight();
        this.width = jpegDirectory.getImageWidth();

    }

    private void readExifMetadata() throws ImageReadException, IOException {
        org.apache.commons.imaging.common.ImageMetadata imageMetadata = (JpegImageMetadata) Imaging.getMetadata(file);
        if (imageMetadata instanceof JpegImageMetadata) {
            JpegImageMetadata metadata = (JpegImageMetadata) imageMetadata;

            this.id = (String) getTagValue(metadata, ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID);

//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            System.out.println(getTagValue(metadata, ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID));

            String dateTimeString = (String) getTagValue(metadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            System.out.println(">>>>> DATE AS A STRING: " + dateTimeString);

//            DateFormat dateFormat = new SimpleDateFormat();

//            try {
            if (dateTimeString != null) {
                String dateString = dateTimeString.substring(0, 10).replace(":", "-");
                String timeString = dateTimeString.substring(11);
                System.out.println("DATE STRING: >" + dateString + "<");
                System.out.println("TIME STRING: >" + timeString + "<");

//                    DateTimeFormatter.ISO_LOCAL_DATE.parse(dateString);

//                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

                LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalTime localTime = LocalTime.parse(timeString);

                photoDateTime = LocalDateTime.of(localDate, localTime);

                System.out.println("DATETIME: " + photoDateTime);


            }

            TiffImageMetadata tiffImageMetadata = metadata.getExif();
            if (tiffImageMetadata != null) {
                TiffImageMetadata.GPSInfo gpsInfo = tiffImageMetadata.getGPS();
                if (gpsInfo != null) {
                    longitude = gpsInfo.getLongitudeAsDegreesEast();
                    latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    System.out.println("GPS!!!!!");
                    System.out.println(longitude);
                    System.out.println(latitude);
                }
            }
//                Date date = dateFormat.parse(dateTimeString);
//                System.out.println(">>>>> DATE AS A STRING: v2: " + date);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            // TODO: 20/07/18 Set up code for extracting longitude and latitude
        } else {
            throw new ImageReadException("Not a Jpeg Image");
        }
    }

    private Object getTagValue(JpegImageMetadata metadata, TagInfo tagInfo) throws ImageReadException {
        TiffField field = metadata.findEXIFValue(tagInfo);
        if (field == null) {
            return null;
        } else return field.getValue();
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
//                System.out.println(tag.g);
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

    public void setId(String id) {
        this.id = id;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public LocalDateTime getPhotoDateTime() {
        return photoDateTime;
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
