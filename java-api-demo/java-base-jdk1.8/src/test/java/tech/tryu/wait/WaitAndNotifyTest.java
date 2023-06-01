package tech.tryu.wait;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class WaitAndNotifyTest {

    /**
     * 1. wait 和 notify 必须在同步代码块中
     * 2. wait 会释放锁资源（同步代码的sync锁资源）
     * 3. 因为，多次连续调用notify()方法并不会叠加通知的效果。每次调用只会唤醒一个线程，而其他线程仍然需要等待合适的时机被唤醒。
     * 如果需要唤醒多个线程，可以考虑使用notifyAll()方法，它会唤醒所有等待在相同对象监视器上的线程。
     * 所以即时多次notify再两次wait，那么wait的线程将永远等待下去
     *
     * @throws InterruptedException
     */
    @Test
    public void waitAndNotifyTest() throws InterruptedException {
        Object lock = new Object();
        // 先等待
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("wait begin");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("wait end");
            }
        }).start();
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("run begin");
                System.out.println("run end");
            }
        }).start();

        // 再通知
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("notify begin");
                lock.notify();
                System.out.println("notify end");
            }
        }).start();

        // 再先通知
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("notify 01 begin");
                lock.notifyAll();
                System.out.println("notify 01 end");
            }
        }).start();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println("wait 02 begin");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("wait 02 end");
            }
        }).start();

        // 看下是否会释放锁
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("running 02 begin");
                System.out.println("running 02 end");
            }
        }).start();

        // 主线程休眠
        TimeUnit.SECONDS.sleep(40);
    }

    /**
     * LockSupport.park() 和 LockSupport.unpark() 不需要在同步代码块中
     * 可以替代wait和notify.
     * 注意：unpark 可以先于 park 执行，但是 unpark 执行多次只会生成一次许可，而 parK() 会消费一次许可
     * 所以如下的例子，线程03永远不会被唤醒
     */
    @Test
    public void supportLockTest() throws InterruptedException {

        // park 等待线程
        Thread thread00 = new Thread(() -> {
            System.out.println("00 begin park");
            LockSupport.park();
            System.out.println("00 end park");
        });
        // park 等待线程
        Thread thread03 = new Thread(() -> {
            System.out.println("03 begin park");
            LockSupport.park();
            System.out.println("03 end park");
        });

        // unPark 线程
        Thread thread01 = new Thread(() -> {
            System.out.println("01 begin unpark");
            LockSupport.unpark(thread00);
            System.out.println("01 end unpark");
        });
        // unPark 线程02
        Thread thread02 = new Thread(() -> {
            System.out.println("02 begin unpark");
            LockSupport.unpark(thread00);
            System.out.println("02 end unpark");
        });

        // 先执行 unPark
        thread01.start();
        thread02.start();
        // 在执行 park
        thread00.start();
        thread03.start();

        // 主线程休眠
        TimeUnit.SECONDS.sleep(10);

    }

}