package tech.tryu.classloader;

import org.junit.Test;

public class ClassLoaderTest {

    /**
     * Class.forName(): 将类的.class文件加载到jvm中之外，还会对类进行解释，执行类中的static块；
     * ClassLoader.loadClass(): 只干一件事情，就是将.class文件加载到jvm中，不会执行static中的内容,
     * 只有在newInstance才会去执行static块。
     * Class.forName(name, initialize, loader)带参函数也可控制是否加载static块。
     * 并且只有调用了newInstance()方法采用调用构造函数，创建类的对象 。
     */
    //使用ClassLoader.loadClass()来加载类，不会执行静态代码块
    @Test
    public void loadClassTest() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        java.lang.ClassLoader loader = Thread.currentThread().getContextClassLoader();
        loader.loadClass("tech.tryu.classloader.ClassLoader");
    }

    //使用Class.forName()来加载类，默认会执行静态代码块
    @Test
    public void loadClassNameTest() throws ClassNotFoundException {
        Class.forName("tech.tryu.classloader.ClassLoader");
    }

    @Test
    //使用Class.forName()来加载类，并指定ClassLoader，初始化时不执行静态块
    public void loadClassNameAndClassLoaderTest() throws ClassNotFoundException {
        Class.forName("tech.tryu.classloader.ClassLoader", false, Thread.currentThread().getContextClassLoader());
    }
}