package tech.tryu.classloader;

/**
 * @author tryu
 */
public class ClassLoader {
    private static int initCount = 0;

    public ClassLoader() {
        System.out.println("ClassLoader constructor");
    }

    static {
        System.out.println("ClassLoader static init");
        initCount++;
    }
}
