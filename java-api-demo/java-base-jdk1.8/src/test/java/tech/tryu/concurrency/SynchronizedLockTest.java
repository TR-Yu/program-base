package tech.tryu.concurrency;

import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


/**
 * @author tryu
 */
public class SynchronizedLockTest {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedLockTest.class);

    /**
     * synchronized 锁的可重入性
     * 理论：
     * 线程在持有锁的状态下，可以多次进入同一个锁的临界区，而不会被自己持有的锁所阻塞
     * 通过基于线程持有锁的标识以及计数器的机制来实现的,每次进入临界区，计数器加一，每次退出临界区，计数器减
     * 是由 synchronized 的实现来保障
     * 通过 MonitorObject 来实现的，每个对象都有一个 MonitorObject.里面有当前持有的线程信息和计数器
     * [来源](http://web.archive.org/web/20230216013349/http://09itblog.site/?p=622)
     *
     */
    @Test
    public void LockTest() {
        Object lock = new Object();
        synchronized (lock) {
            logger.info("Thread {} get lock first", Thread.currentThread().getName());
            System.out.println((ClassLayout.parseInstance(lock).toPrintable()));
            recursiveCall(lock);
        }
        Collection<String> strings = Collections.synchronizedCollection(new LinkedList<>());
    }

    // 递归调用
    public void recursiveCall(Object lock) {
        synchronized (lock) {
            logger.info("Thread {} get lock repeat ", Thread.currentThread().getName());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            System.out.println((ClassLayout.parseInstance(lock).toPrintable()));
            recursiveCall(lock);
        }
    }

    @Test
    public void tryCatchTest() {
        long start = System.nanoTime();
        try {
            DoubleMethod(1_000_000_000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("try catch cost: " + (System.nanoTime() - start));
    }

    @Test
    public void noTryCatchTest() {
        long start = System.nanoTime();
        DoubleMethod(1_000_000_000L);
        System.out.println("no try catch cost: " + (System.nanoTime() - start));
    }

    private Double DoubleMethod(Long times) {

        Double result = 0d;
        for (int i = 0; i < times; i++) {
            Double d = Math.random();
            Double v1 = d * 1000;
            Double v = v1 + 10d;
            Double v2 = v / 10000;
            result += v2;
        }
        return result;
    }
}
