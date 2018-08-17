package client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class RectilinearProjection {

    private static final double PI = Math.PI;
    private static final double HALF_PI = Math.PI / 2;
    private static final double TWO_PI = Math.PI * 2;

    private static final int OUTPUT_WIDTH = 1080;
    private static final int OUTPUT_HEIGHT = 960;

    private static final int FOCAL_LENGTH = 672;

    private final int panoWidth, panoHeight;
    private final double yaw, pitch, roll;

    private final BufferedImage panorama;

    public RectilinearProjection(
            File file,
            double yaw,
            double pitch,
            double roll

    ) throws IOException {
        this.panorama = ImageIO.read(file);
        this.panoWidth = panorama.getWidth();
        this.panoHeight = panorama.getHeight();
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

}
