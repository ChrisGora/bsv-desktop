package client.databaseConnections;

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
import com.google.gson.Gson;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ImageMetadata {

    private static final String TAG = "ImageMetadata";

    private String id;
    private int height;
    private int width;
    private LocalDateTime photoDateTime;
    private double latitude;
    private double longitude;
    private double locationAccuracy;
    private double bearing;
    private double bearingAccuracy;
    private String serialNumber;


    public ImageMetadata(String id, int height, int width, LocalDateTime photoDateTime, double latitude, double longitude, String serialNumber) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.photoDateTime = photoDateTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.serialNumber = serialNumber;
    }

    public ImageMetadata(File image, File jsonInfo) throws IOException, MetadataException, ImageProcessingException, ImageReadException {
        this(image);
        assert (jsonInfo.getName().contains(".json"));
        readJsonMetadata(jsonInfo);
    }

    public ImageMetadata(File file) throws IOException, MetadataException, ImageProcessingException, ImageReadException {
        assert (file.getName().contains(".jpg"));
        readJpegMetadata(file);
        readExifMetadata(file);
    }

    /**
     * Must be called after reading jpeg and exif metadata!
     *
     * @param file Json file to read in
     */
    private void readJsonMetadata(File file) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        ExtraPhotoInfo extraPhotoInfo = new Gson().fromJson(jsonString, ExtraPhotoInfo.class);
        assert (this.id.equals(extraPhotoInfo.getId()));
        this.bearing = extraPhotoInfo.getBearing();
        this.bearingAccuracy = extraPhotoInfo.getBearingAccuracy();
        this.locationAccuracy = extraPhotoInfo.getLocationAccuracy();
    }

    private void readJpegMetadata(File file) throws ImageProcessingException, IOException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        this.height = jpegDirectory.getImageHeight();
        this.width = jpegDirectory.getImageWidth();
    }

    private void readExifMetadata(File file) throws ImageReadException, IOException {
        org.apache.commons.imaging.common.ImageMetadata imageMetadata = (JpegImageMetadata) Imaging.getMetadata(file);
        if (imageMetadata instanceof JpegImageMetadata) {
            JpegImageMetadata metadata = (JpegImageMetadata) imageMetadata;

            this.id = (String) getTagValue(metadata, ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID);

            String dateTimeString = (String) getTagValue(metadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

//            DateFormat dateFormat = new SimpleDateFormat();

//            try {
            if (dateTimeString != null) {
                String dateString = dateTimeString.substring(0, 10).replace(":", "-");
                String timeString = dateTimeString.substring(11);

//                    DateTimeFormatter.ISO_LOCAL_DATE.parse(dateString);

//                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

                LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalTime localTime = LocalTime.parse(timeString);

                photoDateTime = LocalDateTime.of(localDate, localTime);

            }

            TiffImageMetadata tiffImageMetadata = metadata.getExif();
            if (tiffImageMetadata != null) {
                TiffImageMetadata.GPSInfo gpsInfo = tiffImageMetadata.getGPS();
                if (gpsInfo != null) {
                    longitude = gpsInfo.getLongitudeAsDegreesEast();
                    latitude = gpsInfo.getLatitudeAsDegreesNorth();
                }
            }
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

    public void printMetadata(File file) throws ImageProcessingException, IOException, XMPException {
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

    public double getLocationAccuracy() {
        return locationAccuracy;
    }

    public double getBearing() {
        return bearing;
    }

    public double getBearingAccuracy() {
        return bearingAccuracy;
    }
}
