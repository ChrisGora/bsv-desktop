package client.projections;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.*;

public class Projection2Test {

    @Test
    public void firstTest() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("client/test.jpg")).getFile());
        Projection2 p = new Projection2(
                file,
                1200,
                2000,
                500,
                1500,
                70,
                70,
                10,
                10
        );

        p.transform();

    }
}