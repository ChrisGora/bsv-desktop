package client;

import com.google.common.collect.ImmutableList;

import java.util.*;

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

        this.ids = new ArrayList<>();
        this.distances = new HashMap<>();
        this.images = new HashMap<>();
    }

    public void add(ImageMetadata imageMetadata, Double distance) {
        String id = imageMetadata.getId();
        ids.add(id);
        distances.put(id, distance);
        images.put(id, imageMetadata);
    }

    public double getPointLatitude() {
        return pointLatitude;
    }

    public double getPointLongitude() {
        return pointLongitude;
    }

    public List<String> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public Map<String, Double> getDistances() {
        return Collections.unmodifiableMap(distances);
    }

    public Map<String, ImageMetadata> getImages() {
        return Collections.unmodifiableMap(images);
    }
}
