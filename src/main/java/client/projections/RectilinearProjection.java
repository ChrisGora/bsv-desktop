package client.projections;

import static java.lang.Math.*;

import com.drew.lang.GeoLocation;
import javafx.util.Pair;
import org.ejml.simple.SimpleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 *
 * Main algorithm:
 * mathworld.wolfram.com/GnomonicProjection.html
 *
 * Java code largely adapted from the Open Source Geospatial Foundation:
 * https://github.com/geotools/geotools/blob/master/modules/library/referencing/src/main/java/org/geotools/referencing/operation/projection/MapProjection.java
 *
 */
public class RectilinearProjection {

    private static final double PI = Math.PI;
    private static final double HALF_PI = Math.PI / 2;
    private static final double TWO_PI = Math.PI * 2;

    private static final int OUTPUT_WIDTH = 1080;
    private static final int OUTPUT_HEIGHT = 960;

    private static final int FOCAL_LENGTH = 672;

    private static final double EPSILON = 1E-6;

    private final int panoWidth, panoHeight;
    private final double yaw, pitch, roll;


    // Assume the 360 image is a perfect sphere
    private static final double excentricity = 0;

    // (x,y) = (longitude, latitude) = (lambda, phi)

    private final double longitudeOfCentre;     // x, lambda-0, also the central meridian
    private final double latitudeOfCentre;      // y, phi-1, also the equator

    private final double primeVert0;
    private final double projectedCylindricalZ0;

    private final BufferedImage panorama;
    private final File outputFile;

    public RectilinearProjection(
            File file,
            double yaw,
            double pitch,
            double roll,
            double latitudeOfCentre,
            double longitudeOfCentre

    ) throws IOException {
        this.panorama = ImageIO.read(file);
        this.outputFile = new File(file.getParentFile().getAbsolutePath(), file.getName().replace(".jpg", "_PROJECTION.jpg"));

        this.panoWidth = panorama.getWidth();
        System.out.println(panoWidth);
        this.panoHeight = panorama.getHeight();
        System.out.println(panoHeight);

        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;

        this.longitudeOfCentre = longitudeOfCentre;
        this.latitudeOfCentre = latitudeOfCentre;

        this.primeVert0 = 1 / sqrt(1.0 - pow(excentricity, 2) * pow(sin(latitudeOfCentre), 2));
        this.projectedCylindricalZ0 = primeVert0 * sin(latitudeOfCentre);
    }

    public void transformImage() throws IOException {

        final double horizontalFOV = 70;
        final double verticalFOV = 30;
        final double ha = 0;
        final double va = 0;

//        SimpleMatrix rotationY = new SimpleMatrix(
//                {cos()},
//                {},
//                {}
//        );

        BufferedImage output = new BufferedImage(panoWidth, panoHeight, BufferedImage.TYPE_INT_RGB);
        System.out.println(output.getWidth());
        System.out.println(output.getHeight());
//        GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        Image output = GraphicsConfiguration.createCompatibleImage(1000, 1000);

        double[] points = new double[panorama.getWidth() * panorama.getHeight()];
        int n = 0;

        int x = 0;
        while (x < panoWidth) {
            int y = 0;
//            System.out.println("x=" + x);
            while (y < panoHeight) {
//                System.out.println("y=" + y);
                Point2D.Double oldPoint = new Point2D.Double(horizontalFOV * (x - panoWidth / 2) / panoWidth + ha, verticalFOV * (y - panoHeight / 2) / panoHeight + va);
                Point2D.Double newPoint = getTransformedPoint(toRadians(oldPoint.getX()), toRadians(oldPoint.getY()));

//                System.out.println(oldPoint.getX());
//                System.out.println(oldPoint.getY());

                double xCoord = toDegrees((newPoint.getX()) * (panoWidth + ha)) / (horizontalFOV) + panoWidth / 2;
                double yCoord = toDegrees((newPoint.getY()) * (panoHeight + va)) / (verticalFOV) + panoHeight / 2;
                System.out.println(xCoord);
                System.out.println(yCoord);
                if (xCoord > 0 && yCoord > 0 && xCoord < panoWidth && yCoord < panoHeight) {
                    output.setRGB(toIntExact(round(xCoord)), toIntExact(round(yCoord)), panorama.getRGB(x, y));
//                    output.getGraphics().setClip();
                }
                y++;
            }
            x++;
        }

        ImageIO.write(output, "jpg", outputFile);
    }

    private double[] transformCoordinates(double[] sourcePoints, int n) {
        int sourceOffset = 0;
        double[] destinationPoints = new double[n * 2];
        while (n <= 0) {
            Point2D.Double oldPoint = new Point2D.Double(sourcePoints[sourceOffset], sourcePoints[sourceOffset + 1]);
            Point2D.Double newPoint = getTransformedPoint(toRadians(oldPoint.getX()), toRadians(oldPoint.getY()));

            double x = newPoint.getX();
            double y = newPoint.getY();
            if (x != -1 && y != -1) {
                destinationPoints[sourceOffset] = x;
                destinationPoints[sourceOffset + 1] = y;
            }

            sourceOffset = sourceOffset + 2;
            n++;
        }

        return destinationPoints;
    }

    private Point2D.Double getTransformedPoint(double lambda, double phi) {
        final double sinPhi = sin(phi);
        final double cosPhi = cos(phi);
        final double sinLam = sin(lambda);
        final double cosLam = cos(lambda);
        final double sinPhi1 = sin(latitudeOfCentre);
        final double cosPhi1 = cos(latitudeOfCentre);
        final double sinLam0 = sin(longitudeOfCentre);
        final double cosLam0 = cos(longitudeOfCentre);

        final double primeVert = 1 / sqrt(1.0 - pow(excentricity, 2) * pow(sinPhi, 2));
        final double projectedCylindricalZ = primeVert * sinPhi;
        final double projectedCylindricalZDelta = pow(excentricity , 2) * (projectedCylindricalZ - projectedCylindricalZ0);

        final double zFactor = (cosPhi1 * cosPhi * cosLam) + (sinPhi1 * sinPhi);
        if (zFactor <= EPSILON) {
//            System.out.println("ERROR");
            return new Point2D.Double(-1, -1);
        }
        else {
            final double height = (primeVert0 + projectedCylindricalZDelta * sinPhi1) / zFactor;

            final double x = height * cosPhi * sinLam;
            final double y = height * (cosPhi1 * sinPhi - sinPhi1 * cosPhi * cosLam) - projectedCylindricalZDelta * cosPhi1;
            return new Point2D.Double(x, y);
        }
    }
}










    /*

    private SimpleMatrix getRotatedMatrix(double rotateX, double rotateY, double rotateZ) {
        throw new IllegalStateException("Implement me!");
    }

    */
/**
     * Determines and returns corresponding position in the equirectangular panorama
     *//*

    private Pair<Double, Double> getPanoramaPosition(int outputX, int outputY, SimpleMatrix rotationMatrix, SimpleMatrix cameraMatrix) {
//        Also uses focal length (f), pano width (w1), pano height (h1)
        throw new IllegalStateException("Implement me!");
    }

    private double getRadians(double degrees){
        return degrees * PI / 180;
    }
}
*/
