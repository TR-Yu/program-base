package tech.tryu.proxy;

import org.junit.Test;

import java.lang.reflect.Proxy;

public class JdkProxyTest {

    @Test
    public void jdkProxyTest() {
        Object o = Proxy.newProxyInstance(
                A.class.getClassLoader(), new Class[]{A.class},
                (proxy, method, args) -> {
                    try {
                        System.out.println("开始事务....");
                        method.invoke(proxy,args);
                    }catch (Exception e){
                        System.out.println("回滚事务....");
                    }finally {
                        System.out.println("提交事务....");
                    }
                    return null;
                });
        ((A) o).m(null);
    }
}
interface A {
    default String m(A a) {
        System.out.println("A");
        return "t1";
    }
}
interface B {
    default String m(A a) {
        System.out.println("B");
        return "t2";
    }
}
interface C extends A {}
