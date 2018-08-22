package client;

import client.projections.RectilinearProjection;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class RectilinearProjectionTest {

    @Test
    public void transformImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());
        RectilinearProjection projection = new RectilinearProjection(file, 0, 0, 0, 500, 500);
        projection.transformImage();
    }
}