package client;

import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;

public class ImageMetadataTest {

    @Test
    public void fileReadTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());
        ImageMetadata metadata = new ImageMetadata(file);

        int width = metadata.getWidth();
        int height = metadata.getHeight();
        String id = metadata.getId();
        String serialNumber = metadata.getSerialNumber();

        assertEquals(5376, width);
        assertEquals(2688, height);
        assertEquals("0236451263344ab88f9940679b1dc59b", id);
//        assertEquals();

    }
}