package tech.tryu.concurrency;

import org.junit.Test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

    // region ReentrantLock 顺序死锁
    /**
     * 重入锁，两个线程的顺序死锁案例，使用 synchronized 修饰方法，会出现死锁
     * 解决方案：
     * 1. 让获取锁的执行顺序一致，例如都是先获取 lock01, 再获取 lock02
     * 2. 有限时地获取锁，当获取锁超时后，放弃获取锁，继续执行其他逻辑来缓解死锁问题，
     * 但是不能解决一个线程获取锁的时间过长导致其他线程一直等待的问题
     *
     * @throws InterruptedException
     */
    @Test
    public void ReentrantDeadLockTest() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();
        Thread thread01 = new Thread(() -> {
            lock1.lock();
            lock2.lock();
            LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(10));
            lock1.unlock();
            lock2.unlock();
        });
        thread01.start();

        Thread thread02 = new Thread(() -> {
            lock2.lock();
            lock1.lock();
            LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(10));
            lock2.unlock();
            lock1.unlock();
        });
        thread02.start();

        thread01.join();
        thread02.join();
    }
    // endregion

    // region ReentrantLock 顺序死锁解决方案
   /**
    *采用立即获取锁的方式，如果获取不到锁，则放弃获取锁，自己控制逻辑再次获取锁，但是还是不够灵活
    *采用限时获取锁的方式，评估业务执行的时间，再次获取锁的时候，如果超时，则放弃获取锁。相对来说，这种方式更灵活
    *注意：锁是不会抛出异常的，所以需要自己控制逻辑
    */
    @Test
    public void SolveReentrantDeadLockTest() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock(true);
        ReentrantLock lock2 = new ReentrantLock(true);
        AtomicInteger count01 = new AtomicInteger(0);
        Runnable taskRunnable = () -> {
            try {
                boolean flag01 = lock1.tryLock(10, TimeUnit.SECONDS);
                boolean flag02 = lock2.tryLock(10, TimeUnit.SECONDS);
                if (!flag01 || !flag02) {
                    System.out.println("获取锁超时");
                    return;
                }
                count01.incrementAndGet();
                System.out.println("taskRunnable" + "lock1 = " + lock1.isHeldByCurrentThread() + " lock2 = " + lock2.isHeldByCurrentThread() + " count01 = " + count01);
                doSomething(TimeUnit.MINUTES.toNanos(1));
                System.out.println("end");
            } catch (InterruptedException e) {
                System.out.println("获取锁超时" + e); // 记录获取超时的日志
            } finally {
                if (lock1.isHeldByCurrentThread()) {
                    lock1.unlock();
                }
                if (lock2.isHeldByCurrentThread()) {
                    lock2.unlock();
                }
            }
        };
        Runnable taskRunnable02 = () -> {
            try {
                boolean flag01 = lock1.tryLock(10, TimeUnit.SECONDS);
                boolean flag02 = lock2.tryLock(10, TimeUnit.SECONDS);
                if (!flag01 || !flag02) {
                    System.out.println("获取锁超时");
                    return;
                }
                count01.incrementAndGet();
                System.out.println("taskRunnable01" + "lock1 = " + lock1.isHeldByCurrentThread() + " lock2 = " + lock2.isHeldByCurrentThread() + " count01 = " + count01);
                doSomething(TimeUnit.MINUTES.toNanos(1));
                System.out.println("end");
            } catch (InterruptedException e) {
                System.out.println("获取锁超时" + e); // 记录获取超时的日志
            } finally {
                if (lock1.isHeldByCurrentThread()) {
                    lock1.unlock();
                }
                if (lock2.isHeldByCurrentThread()) {
                    lock2.unlock();
                }
            }
        };

        new Thread(taskRunnable).start();
        new Thread(taskRunnable02).start();
        new Thread(taskRunnable).start();
        new Thread(taskRunnable02).start();
        new Thread(taskRunnable).start();

        TimeUnit.HOURS.sleep(100);
        //
    }

    private void doSomething(long longTime) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < longTime) {
            // 一直循环，直到超时
        }
    }

    // endregion

    // region Get CPU Info
    /**
     * 这是一个在 Windows 和 Linux 上都可以使用的获取 CPU 使用率的方法
     * 但是注意，采用 ManagementFactory.getOperatingSystemMXBean() 获取到的是 com.sun.management.OperatingSystemMXBean
     * 需要转化为 com.sun.management.OperatingSystemImpl 才能获取到 ProcessCpuLoad 属性
     * 或是实际获取到的对象实例化，然后从属性中获取 ProcessCpuLoad 属性值
     */
    @Test
    public void getProcessCpuLoadTest() {
        try {

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            Double cpUse = Optional.ofNullable(list)
                    .map(l -> l.isEmpty() ? null : l)
                    .map(List::iterator)
                    .map(Iterator::next)
                    .map(Attribute.class::cast)
                    .map(Attribute::getValue)
                    .map(Double.class::cast)
                    .orElse(null);

            System.out.println("ProcessCpuLoad : " + cpUse);
        } catch (Exception ex) {
            System.out.println("error" + ex);
        }
    }
    // endregion

}


