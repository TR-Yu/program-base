package tech.tryu.spi;

/**
 * 文本类型数据流 读取
 * @author tryu
 */
public class TextStream implements StreamSPI{
    @Override
    public void read() {
        System.out.println("text stream read");
    }
}
