package client.projections;

import org.ejml.data.Matrix;
import org.ejml.simple.SimpleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.*;

public class Projection2 {

    private File originalFile;
    private BufferedImage originalImage;

    // Photo resolution
    private int imageWidthPixels; //img_w_px
    private int widthStart;
    private int widthEnd;
    private int imageHeightPixels; //img_h_px
    private int heightStart;
    private int heightEnd;

    // Camera FOV angles
    private double horizontalFovDegrees; //img_ha_deg
    private double verticalFovDegrees; //img_va_deg

    // Camera rotation angles in degrees
    private double horizontalCameraRotationDegrees; //hcam_deg
    private double verticalCameraRotationDegrees; //vcam_deg

    // Camera rotation angles in radians
    private double horizontalCameraRotationRadians; //hcam_rad
    private double verticalCameraRotationRadians; //vcam_rad

    // Horizontal rotation of the camera (around the z-axis)
    private SimpleMatrix zRotationMatrix; //rot_z

    // Vertical rotation of the camera (around the y-axis)
    private SimpleMatrix yRotationMatrix; //rot_y


    public Projection2(
            File file,
            int startX,
            int endX,
            int startY,
            int endY,
            double horizontalFovDegrees,
            double verticalFovDegrees,
            double horizontalCameraRotationDegrees,
            double verticalCameraRotationDegrees
    ) {
        this.originalFile = file;
        this.horizontalFovDegrees = horizontalFovDegrees;
        this.verticalFovDegrees = verticalFovDegrees;
        this.horizontalCameraRotationDegrees = horizontalCameraRotationDegrees;
        this.verticalCameraRotationDegrees = verticalCameraRotationDegrees;
        setImageWidth(startX, endX);
        setImageHeight(startY, endY);
        setAnglesInRadians();
        setHorizontalRotationMatrix();
        setVerticalRotationMatrix();
    }


    public void transform() throws IOException {


        int geoW = 500;
        int geoH = 500;

        BufferedImage oldImage = ImageIO.read(originalFile);
        BufferedImage newImage = new BufferedImage(geoW, geoH, BufferedImage.TYPE_INT_RGB);

        int x = 0;
        while (x < imageWidthPixels) {
            int y = 0;
            while (y < imageHeightPixels) {
                int rgb = oldImage.getRGB(adjustX(x), adjustY(y));

                double unknown1 = horizontalFovDegrees;
                double pTheta = (y - imageWidthPixels / 2.0) / imageWidthPixels * toRadians(unknown1);
                double unknown2 = verticalFovDegrees;
                double pPhi = -(x - imageHeightPixels / 2.0) / imageHeightPixels * toRadians(unknown2);

                double pX = cos(pPhi) * cos(pTheta);
                double pY = cos(pPhi) * sin(pTheta);
                double pZ = sin(pPhi);
                SimpleMatrix vectorP0 = new SimpleMatrix(new double[][]{
                        {pX},
                        {pY},
                        {pZ}
                });

                SimpleMatrix vectorP1 = yRotationMatrix.mult(vectorP0);
                SimpleMatrix vectorP2 = zRotationMatrix.mult(vectorP1);

                double theta = atan2(vectorP2.get(1), vectorP2.get(0));
                double phi = asin(vectorP2.get(2));

                double longitude = toDegrees(theta);
                double latitude = toDegrees(phi);


                int geoPixelX = getInt((longitude + 180) * geoW / 360);
                int geoPixelY = getInt((latitude + 90) * geoH / 360);

//                System.out.println(geoPixelX);
//                System.out.println(geoPixelY);

                if (geoPixelX < geoW && geoPixelY < geoH) {
                    newImage.setRGB(geoPixelX, geoPixelY, rgb);
                }

                y++;
            }
            x++;
        }

        File outputFile = new File(originalFile.getParentFile().getAbsolutePath(), originalFile.getName().replace(".jpg", "_PROJECTION.jpg"));
        ImageIO.write(newImage, "jpg", outputFile);


    }

    private int getInt(double d) {
        return toIntExact(round(d));
    }

    private void setImageWidth(int x1, int x2) {
        if (x1 < x2) {
            widthStart = x1;
            widthEnd = x2;
        } else if (x1 > x2) {
            widthStart = x2;
            widthEnd = x1;
        } else {
            throw new IllegalArgumentException("Image width is 0");
        }

        imageWidthPixels = widthEnd - widthStart;
    }

    private void setImageHeight(int y1, int y2) {
        if (y1 < y2) {
            heightStart = y1;
            heightEnd = y2;
        } else if (y1 > y2) {
            heightStart = y2;
            heightEnd = y1;
        } else {
            throw new IllegalArgumentException("Image height is 0");
        }

        imageHeightPixels = heightEnd - heightStart;
    }

    private void setAnglesInRadians() {
        horizontalCameraRotationRadians = toRadians(horizontalCameraRotationDegrees);
        verticalCameraRotationRadians = toRadians(verticalCameraRotationDegrees);
    }

    private void setHorizontalRotationMatrix() {
        double data[][] = {
                {cos(verticalCameraRotationRadians), 0, sin(verticalCameraRotationRadians)},
                {0, 1, 0},
                {-sin(verticalCameraRotationRadians), 0, cos(verticalCameraRotationRadians)}
        };

        zRotationMatrix = new SimpleMatrix(data);
    }

    private void setVerticalRotationMatrix() {
        double[][] data = {
                {cos(horizontalCameraRotationRadians), -sin(horizontalCameraRotationRadians), 0},
                {sin(horizontalCameraRotationRadians), cos(horizontalCameraRotationRadians), 0},
                {0, 0, 1}
        };

        yRotationMatrix = new SimpleMatrix(data);
    }

    private int adjustX(int x) {
        return x + widthStart;
    }

    private int adjustY(int y) {
        return y + heightStart;
    }
}
