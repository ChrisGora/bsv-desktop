package client;

public class PhotoResult {

    private final String id;
    private final double distance;
    private final ImageMetadata imageMetadata;

    public PhotoResult(String id, double distance, ImageMetadata imageMetadata) {
        this.id = id;
        this.distance = distance;
        this.imageMetadata = imageMetadata;
    }

    public String getId() {
        return id;
    }

    public double getDistance() {
        return distance;
    }

    public ImageMetadata getImageMetadata() {
        return imageMetadata;
    }
}
