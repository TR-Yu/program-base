package tech.tryu.spi;

import org.junit.Test;

import java.util.ServiceLoader;

import static org.junit.Assert.*;

public class LoadStreamTest {

    @Test
    public void testLoadStream() {
        ServiceLoader<StreamSPI> serviceLoader = ServiceLoader.load(StreamSPI.class);
        for (StreamSPI streamSPI : serviceLoader) {
            streamSPI.read();
        }
    }

}