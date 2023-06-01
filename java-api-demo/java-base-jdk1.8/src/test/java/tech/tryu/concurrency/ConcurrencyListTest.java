package tech.tryu.concurrency;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class ConcurrencyListTest {

    /**
     * 线程池内的已执行的任务，在不响应中断的情况下，会一直执行
     * 所以，要是想要线程池中的任务能够响应中断，需要在任务中自己判断中断状态，
     * 需要根据业务的场景来决定是否响应中断，防止任务一直运行中。
     */
    @Test
    public void threadPoolTest() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(() -> {
            for (;;) {
                System.out.println("Hello World");
            }
        });
        executorService.shutdownNow();

        // 防止在测试时主线程退出后，子线程也退出,默认主线程等待 20 mines
        LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(20));
    }

    /**
     * 测试线程池中的任务抛出异常后，设置了线程的 UncaughtExceptionHandler 后，是否会执行
     * 注意提交的任务是 Runnable，而不是 Callable， Callable 的异常会被 Future.get() 捕获导致线程池中的 UncaughtExceptionHandler 不会执行
     * 第二种处理方式是，重写 ExecutorService 的 afterExecute 方法，对执行后的异常进行处理
     * 注意 beforeExecute 方法的线程执行方法时抛出异常则不会执行到 run 方法，也就不会执行 afterExecute 方法
     *
     */
    @Test
    public  void threadFactoryTest(){
        int cpuThread = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cpuThread, cpuThread * 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "tryu-thread-" + threadNumber.getAndIncrement());
                        thread.setUncaughtExceptionHandler((t, e) -> System.out
                                .println("线程:" + t.getName() + "发生异常:" + e.toString() + "Group" + t.getThreadGroup()));
                        return thread;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());

        threadPoolExecutor.execute(() -> {
            int i = 1 / 0;
        });
        threadPoolExecutor.shutdown();
        // 防止在测试时主线程退出后，子线程也退出,默认主线程等待 1 minutes
        LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(1));
    }
}