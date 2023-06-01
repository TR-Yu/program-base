package tech.tryu.spi;

/**
 * @author tryu
 */
public class VideoStream implements StreamSPI{
    @Override
    public void read() {
        System.out.println("video stream read");
    }
}
