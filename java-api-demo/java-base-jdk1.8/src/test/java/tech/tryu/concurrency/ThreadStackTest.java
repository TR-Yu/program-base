package tech.tryu.concurrency;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 线程栈的相关行为
 * @author tryu
 */
public class ThreadStackTest {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(ConcurrencyMapTest.class);
    private final static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private final static InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

    //region ThreadLocal and inheritableThreadLocal
    @Test
    public void threadLocalTest() {
            // 在主线程中设置 ThreadLocal 的值
            threadLocal.set("Hello from main thread");
            inheritableThreadLocal.set("Hello from main thread");
            // 创建两个子线程并启动
            Thread thread1 = new Thread(() -> {
                // 从子线程1中访问 ThreadLocal 的值
                Assert.assertNull(threadLocal.get());
                Assert.assertEquals("Hello from main thread", inheritableThreadLocal.get());
                // 在子线程1中设置 ThreadLocal 的值
                threadLocal.set("Hello from thread 1");
                inheritableThreadLocal.set("Hello from thread 1");
                // inheritableThreadLocal 在子线程中被修改后，则取到的是子线程中的值，其它线程取到的还是主线程中的值
                // 相对于主线程的给了一个初始值，子线程的修改不会影响到主线程的，同时主线程修改也不会影响到子线程的
                Assert.assertEquals("Hello from thread 1", threadLocal.get());
                Assert.assertEquals("Hello from thread 1", inheritableThreadLocal.get());
            });

            Thread thread2 = new Thread(() -> {
                // 从子线程2中访问 ThreadLocal 的值
                Assert.assertNull(threadLocal.get());
                Assert.assertEquals("Hello from main thread", inheritableThreadLocal.get());
                // 在子线程2中设置 ThreadLocal 的值
                threadLocal.set("Hello from thread 2");
                Assert.assertEquals("Hello from main thread", inheritableThreadLocal.get());
                Assert.assertEquals("Hello from thread 2", threadLocal.get());
                // 阻塞两秒钟，等待主线程修改 inheritableThreadLocal 的值
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
                Assert.assertEquals("Hello from main thread", inheritableThreadLocal.get());
            });

            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            inheritableThreadLocal.set("Hello from main thread 02");
            // 启动两个子线程
            thread1.start();
            thread2.start();

            // 因为在测试多线程的时候，主线程会退出，所以需要让主线程阻塞一段时间，等待子线程执行完毕默认执行时间为 10 s
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
    }

    // endregion

    // region LockSupport vs sleep
    /**
     * 1. sleep LockSupport.parkNanos 都会释放 CPU 资源,但是不会释放锁资源
     * 2. sleep 会抛出 InterruptedException，而 LockSupport.parkNanos 不会抛出异常 看是否抛出中断异常
     */
    @Test
    public void LocalSupportVSleep(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(() -> {
            try {
                synchronized (this) {
                    System.out.println("sleep before");
                    TimeUnit.SECONDS.sleep(60);
                    System.out.println("sleep after");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        executorService.submit(() -> {
            synchronized (this) {
                System.out.println("park before");
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(60));
                System.out.println("park after");
            }
        });

        //因为在测试多线程的时候，主线程会退出，所以需要让主线程阻塞一段时间，等待子线程执行完毕默认执行时间为 10 s
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(120));
    }
    //endregion

    // region priorityBlockingQueue 优先级队列
    @Test
    public void PriorityTaskTest() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10,
                0, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(), r -> new Thread(r, "PriorityTask"),
                new ThreadPoolExecutor.AbortPolicy());

        threadPoolExecutor.submit(new PriorityTask("task1", 3, () -> System.out.println("task1")));
        threadPoolExecutor.submit(new PriorityTask("task2", 2, () -> System.out.println("task2")));
        threadPoolExecutor.submit(new PriorityTask("task3", 0, () -> System.out.println("task3")));

        threadPoolExecutor.shutdown();
        // 因为在测试多线程的时候，主线程会退出，所以需要让主线程阻塞一段时间，等待子线程执行完毕默认执行时间为 10 s
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(120));
    }
    static class PriorityTask implements Comparable<PriorityTask> , Runnable{
        private final String name;
        private final int priority;
        private final Runnable runnable;

        public PriorityTask(String name, int priority, Runnable runnable) {
            this.name = name;
            this.priority = priority;
            this.runnable = runnable;
        }
        @Override
        public void run() {
               runnable.run();
               logger.info("task {} finished", name);
        }
        @Override
        public int compareTo(PriorityTask o) {
            return Integer.compare(this.priority, o.priority);
        }

   }

   // endregion

    //region SynchroniseQueue 同步队列

    /**
     * 行为：如果一个线程试图将一个元素放入队列中，则直接递交给子线程完成，
     * 如果没有子线程，则会按照指定的拒绝策略进行处理默认的拒绝策略是抛出异常
     * 这种一般用的很少
     * 还有 CallerRunsPolicy 也是一种拒绝策略，但是不会抛出异常，而是由主线程来执行
     */
    @Test
    public void SynchroniseQueueTest() {
        SynchronousQueue<Runnable> queue = new SynchronousQueue<>();
        ThreadPoolExecutor synchroniseExecutor =
                new ThreadPoolExecutor(1, 1, 0,
                                        TimeUnit.SECONDS, queue, r -> new Thread(r, "SynchroniseQueue"),
                                        new ThreadPoolExecutor.CallerRunsPolicy());

        synchroniseExecutor.submit(() -> {
            TimeUnit.SECONDS.sleep(5);
            logger.info("{} task01 finished", Thread.currentThread().getName());
            return "task01";
        });
        synchroniseExecutor.submit(() -> {
            TimeUnit.SECONDS.sleep(10);
            logger.info("{} task02 finished", Thread.currentThread().getName());
            return "task02";});

        synchroniseExecutor.submit(() -> {
            TimeUnit.SECONDS.sleep(15);
            logger.info("{} task03 finished", Thread.currentThread().getName());
            return "task03";});
        synchroniseExecutor.submit(() -> {
            TimeUnit.SECONDS.sleep(20);
            logger.info("{} task04 finished", Thread.currentThread().getName());
            return "task04";});

        synchroniseExecutor.shutdown();
        // 因为在测试多线程的时候，主线程会退出，所以需要让主线程阻塞一段时间，等待子线程执行完毕默认执行时间为 10 s
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30));

    }

    /**
     * 生产者一个，消费者 两个，当生产者生产一个元素的时候，消费者会消费这个元素，
     * 消费者的消费速度比生产者的生产速度快，那么生产者会阻塞，直到消费者消费完毕，而不会生产者会一直生产
     * 从而控制了生产者和消费者的速度
     */
    @Test
    public void QueueRunTest(){

        // 阻塞队列无存储的能力
        SynchronousQueue<String> queue = new SynchronousQueue<>();

        // 生产者线程
        Thread producer = new Thread(() -> {
            try {
                Thread.currentThread().setName("producer");
                while (!Thread.currentThread().isInterrupted()) {
                    String value = String.valueOf(System.nanoTime());
                    queue.put(value);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        // 消费者线程 01
        Thread consumer01 = new Thread(() -> {
            try {
                Thread.currentThread().setName("consumer01");
                while (!Thread.currentThread().isInterrupted()) {
                    String take = queue.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // 消费者线程 02
        Thread consumer02 = new Thread(() -> {
            try {
                Thread.currentThread().setName("consumer02");
                while (!Thread.currentThread().isInterrupted()) {
                    String take = queue.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.start();
        consumer01.start();
        consumer02.start();

        // 主线程睡眠 60s 之后，中断所有线程, 主要是验证，在消费者的消费能力大于生产者的生产能力的时候，生产者会 Parking
        // 发现消费者也会 parking,并且存在消费者和生产者都 parking 的现状
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(60));
        String[] array = new String[]{"1", "2", "3"};
        Arrays.stream(array).forEach(System.out::println);
    }
    //endregion

    @Test
    public void FutureTaskTest(){
        FutureTask<String> futureTask = new FutureTask<String>(() -> {
            System.out.println("futureTask01 start");
            while(!Thread.currentThread().isInterrupted()){
                System.out.println("futureTask01 running");            }
            System.out.println("futureTask01 end");
            return "futureTask01";
        });
        Thread thread = new Thread(futureTask);
        thread.start();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        thread.interrupt();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(360));
    }

}