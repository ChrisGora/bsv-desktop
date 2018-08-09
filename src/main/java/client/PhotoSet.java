package client;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoSet {

    private double pointLatitude;
    private double pointLongitude;

//    Image IDs, closest images first, furthest images last
    private List<String> ids;

//    Map between Image IDs and distances from the specified point
    private Map<String, Double> distances;

//    Map between Image IDs and their metadata
    private Map<String, ImageMetadata> images;

    public PhotoSet(double pointLatitude, double pointLongitude) {
        this.pointLatitude = pointLatitude;
        this.pointLongitude = pointLongitude;
    }

    public double getPointLatitude() {
        return pointLatitude;
    }

    public double getPointLongitude() {
        return pointLongitude;
    }

    public List<String> getIds() {
        return ids;
    }

    public Map<String, Double> getDistances() {
        return distances;
    }

    public Map<String, ImageMetadata> getImages() {
        return images;
    }
}
