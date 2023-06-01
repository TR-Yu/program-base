package tech.tryu.concurrency;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class CompleteFutureDemosTest {
    private static final Logger logger = LoggerFactory.getLogger(CompleteFutureDemosTest.class);

    /**
     * CompletableFuture 实现了 Future 接口，和 CompletionStage 接口
     *
     * 1. 什么是 CompletableFuture ？
     * 总的来说，CompletableStage 是一种处理任务的一种模式。是 Reactive Programming 的一种实现。简化了异步编程。
     * 每个 CompletableStage 是一个执行链中的一个元素，它的结果会传递给下一个 CompletableStage。
     * "上游" CompletableStage 的结果会传递给 "下游" CompletableStage。
     * 而 CompletableFuture 是 CompletableStage 的一个具体实现。
     *
     * 2. CompletableFuture 有哪些方法？
     *
     *  Future 接口定义方法：
     *  cancel()：取消任务;
     *  isDone()：任务是否完成; isCancelled()：任务是否被取消;
     *  get()：获取任务的结果; get(long timeout, TimeUnit unit)：获取任务的结果，如果超时，抛出 TimeoutException 异常;
     *
     * 类似 Future 的定义方法：
     *  not Force the future to complete:
     *  join()：获取任务的结果，如果任务没有完成，阻塞等待;getNow()：获取任务的结果，如果任务没有完成，返回指定的值;
     *
     *  Force the future to complete:
     *  complete()：设置未完成的任务的结果，如果任务已经完成，抛出 IllegalStateException 异常;会被已完成的结果覆盖
     *  obtrude()：设置已完成的任务的结果，常用于错误恢复解决方法上
     *
     *  1. 创建 CompletableFuture 对象 ：
     *   创建CompletableFuture对象,空或是已经完成的
     *      new CompletableFuture()：创建一个空的 CompletableFuture 对象
     *      completedFuture()：创建一个已经完成的 CompletableFuture 对象
     *   从任务 Task 中创造 CompletableFuture 对象
     *      supplyAsync()：创建一个异步任务，该任务会在一个新的线程中执行
     *      runAsync()：创建一个异步任务，该任务会在一个新的线程中执行
     *
     *  2. CompletableFuture 链构建的元素：
     *     Four possible functions：
     *     Takes a parameter   Returns void    Returns R
     *       Takes T           Consumer<T>   Function<T, R>
     *  Does not take anything Runnable    Not an element of a chain
     *
     *     Types of chaining : one-to-one chaining composition
     *     Satisfy the ExecutorService
     *
     *  3. One-to-one Patterns
     *   methodName: then-xxx
     *   xxx : run for Runnable, accept for Consumer, and apply for Function
     *   further suffix : Async.  An async method executes its task in the default fork/join pool,
     *   unless it takes an Executor, in which case, the task will be executed in this Executor.
     *  4. two-to-one Patterns
     *   <U, R> CompletionStage<R> thenCombine(CompletionStage<U> other, BiFunction<T, U, R> action)
     *   <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<U> other, BiConsumer<T, U> action)
     *   <U> CompletionStage<Void> runAfterBoth(CompletionStage<U> other, BiConsumer<T, U> action)
     *  5. one-to-two Patterns
     *  <U> CompletionStage<U> applyToEither(CompletionStage<T> other, Function<T, U> action)
     *  <U> CompletionStage<U> acceptEither(CompletionStage<T> other, Consumer<T> action)
     *  <U> CompletionStage<Void> runAfterEither(CompletionStage<T> other, Runnable action)
     *  6. Exception Handling
     *  <U> CompletionStage<U> exceptionally(Function<Throwable, ? extends U> fn)
     *      upstream is completed completed normally, then the downstream is completed normally with the same value.
     *      upstream is completed exceptionally, then this exception is passed to the provided function to produce a result.
     *  <U> CompletionStage<U> handle(BiFunction<T, Throwable, ? extends U> fn)
     *     upstream is completed completed normally, the function is invoked with the result and null as arguments.
     *     upstream is completed exceptionally, the function is invoked with null and the Throwable as arguments.
     *     return the result of the function
     *  <U> CompletionStage<U> whenComplete(BiConsumer<T, Throwable> action)
     *      follows the behavior of the CompletionStage it is built on. 返回的值是 upstream 的值 null 或者是 upstream 的异常
     *      the action is invoked with the result and null as arguments if the upstream is completed normally.
     *      the action is invoked with null and the Throwable as arguments if the upstream is completed exceptionally.
     *
     * 参考：
     * https://forums.oracle.com/ords/apexds/post/completablefuture-for-asynchronous-programming-in-java-8-8539
     */

    // region create CompletableFuture
    /**
     *  1. Creating a Completed CompletableFuture
     *  CompletableFuture.completedFuture()：创建一个已经完成的 CompletableFuture 对象
     *  new CompletableFuture()：创建一个空的 CompletableFuture 对象
     *
     *  2. Creating a CompletableFuture from a Task
     *  CompletableFuture.supplyAsync()：创建一个异步任务，该任务会在一个新的线程中执行
     *  CompletableFuture.runAsync()：创建一个异步任务，该任务会在一个新的线程中执行
     *
     *  3. notes:
     *  CompletableFuture 未指定线程池的情况下，它会使用 ForkJoinPool.commonPool() 作为它的线程池，
     *  但是它的线程池的线程为 demon 线程，所以如果主线程结束，它的线程池中的线程也会结束。可能会导致异步任务无法执行。
     *  所以，我们需要自己创建一个线程池，然后将其作为 CompletableFuture 的线程池。手动创建的线程池的线程默认为非 demon 线程。
     */
    @Test
    public void newOrCompleteMethodTest() {
        /*
         * 两者的区别在于，uncompletedFuture 是一个空的 CompletableFuture 对象，它的结果需要手动设置，
         * 而 completableFuture 是一个已经完成的 CompletableFuture 对象，它的结果是 "hello"。
         * 测试方法：使用 get() 方法获取结果的时候，
         * uncompletedFuture.get() 会阻塞等待，而 completableFuture.get() 不会阻塞。
         */
        CompletableFuture<String> uncompletedFuture = new CompletableFuture<>();
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("hello");

        try {
            logger.info("completedFuture.get() start");
            logger.info("completedFuture.get( {} ) end", completableFuture.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("uncompletedFuture.get() interrupted");
        } catch (ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.info("uncompletedFuture.get() execution exception");
        }

        try {
            logger.info("uncompletedFuture.get() start");
            uncompletedFuture.get();
            logger.info("uncompletedFuture.get() end");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("uncompletedFuture.get() interrupted");
        } catch (ExecutionException e) {
            Thread.currentThread().interrupt();
            logger.info("uncompletedFuture.get() execution exception");
        }
    }
    @Test //在 test 中需要主线程等待，否则会直接返回，中断测试 该例子中采用的是 join() 方法
    public void synCreateCompleteFuture() {

        /*
         * 返回一个 CompletableFuture 对象，它的结果是通过一个异步任务计算出来的。
         * supplyAsync() 方法会创建一个异步任务，该任务会在一个新的线程中执行
         * runAsync() 方法会创建一个异步任务，该任务会在一个新的线程中执行，但是不会返回结果
         * 两者都有一个重载方法，可以指定线程池。
         * get()：如果任务没有完成，get() 方法会阻塞等待任务完成，然后返回结果, 如果任务抛出异常，get() 方法会抛出异常。
         * join()：如果任务没有完成，join() 方法会阻塞等待任务完成，然后返回结果, 如果任务抛出异常，join() 方法会抛出 CompletionException 异常。
         * getNow(default value)：如果任务没有完成，getNow() 方法会直接返回指定的 default value。
         *
         */
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> "hello");
        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> System.out.println("hello"));

        // supplyAsync 示例
        ExecutorService executorService = new ThreadPoolExecutor(
                10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                (r) -> {
                    Thread t = new Thread(r);
                    t.setName("my-thread");
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());

        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                    Thread.currentThread().getName(), Thread.currentThread().getId(),
                    Thread.currentThread().isDaemon());
            return "hello";}, executorService);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                    Thread.currentThread().getName(), Thread.currentThread().getId(),
                    Thread.currentThread().isDaemon());
            return "hello";}, executorService);

        CompletableFuture<String> resultFuture = future01.thenCombine(future02, (s1, s2) -> {
            logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                    Thread.currentThread().getName(), Thread.currentThread().getId(),
                    Thread.currentThread().isDaemon());
            return s1 + " " + s2;
        });

        String result = null;
        resultFuture.thenAccept(s -> {
            logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                    Thread.currentThread().getName(), Thread.currentThread().getId(),
                    Thread.currentThread().isDaemon());

            s = s + " world";
        });
        CompletableFuture<String> fc = CompletableFuture
                .supplyAsync(() -> {
                    logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                            Thread.currentThread().getName(), Thread.currentThread().getId(),
                            Thread.currentThread().isDaemon());
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
                    return "hello";}, executorService)
                .thenApplyAsync(s -> {
                    logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                            Thread.currentThread().getName(), Thread.currentThread().getId(),
                            Thread.currentThread().isDaemon());
                    return s + " world";})
                .exceptionally(e -> {
                    logger.info("\nsupplyAsync: threadName: {}\t threadId: {}\t isDaemon: {}",
                            Thread.currentThread().getName(), Thread.currentThread().getId(),
                            Thread.currentThread().isDaemon());
                    return "error";});

        fc.join();
    }
    // endregion

    // region CompletableFuture chaining methods one to one
    /**
     * 测试 join() 是否会阻塞
     * 在 @test 下无法测试，因为主线程会直接返回，中断测试
     */
    public static void main(String[] args) {
        new CompleteFutureDemosTest().joinMethodTest();
    }

    private void joinMethodTest() {

        long startTime = System.currentTimeMillis();
        Exchanger<String> exchanger = new Exchanger<>();


        ExecutorService executorService = new ThreadPoolExecutor(
                10, 10,
                10L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                (r) -> {
                    Thread t = new Thread(r);
                    t.setName("my-thread");
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());

        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task1");
            System.out.println(Thread.currentThread().getName()+ Thread.currentThread().getId());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(20));
            try {
                String exchangeStr = exchanger.exchange("world");
                return exchangeStr; // "hello"
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        },executorService);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task2");
            System.out.println(Thread.currentThread().getName() + Thread.currentThread().getId());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            try {
                String exchangeStr = exchanger.exchange("hello");
                return exchangeStr;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(future01, future02);
        CompletableFuture<String> task2 = allOf.thenApply((v) -> {
            System.out.println("merge task");
            System.out.println(Thread.currentThread().getName() + Thread.currentThread().getId());
            String s1 = future01.join();
            String s2 = future02.join();
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            String s21 = s1 + " " + s2;
            System.out.println(s21);
            return s21;
        });
        executorService.shutdown();
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
    }
    // endregion


    /**
     * 参考：https://java.jverson.com/juc/completefuture.html
     * thenApply() , thenRun() , thenAccept(),执行的线程是是否为上一个任务运行的线程？
     * 若是上一个是异步任务，则是异步任务的线程，若是上一个是同步任务，则是同步任务的线程
     * 有一个非常特别的注意要点，当上一个任务是 supplyAsync() 时，
     * 测试结果为若是执行时间大于 1 ns 则是异步任务的线程，若是执行时间小于 1 ns ，否则运行还是原来的 main 线程
     * 原因：一个 future 如果在计算完成之后再调用 thenApply，那么就会使用客户端线程（即主线程）继续执行，
     * 但是如果说 supplyAsync 里执行时间较长，可以在其执行完成之前就注册了 thenApply，
     * 那么 thenApply 的计算将使用 supplyAsync 同样的线程。
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void threadSourceTest() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("begin: " + Thread.currentThread().getName());

        CompletableFuture<Void> applyCompletableFuture = CompletableFuture.supplyAsync(() -> {
             long start = System.nanoTime();
            System.out.println("a: " + Thread.currentThread().getName());
/*            LockSupport.parkNanos(1); // 模拟耗时操作
            System.out.println("time = " + (System.nanoTime() - start) + "ns");*/
            return 0;
        }, executor).thenApply(x -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(500));
            System.out.println("b: " + Thread.currentThread().getName());
            return x + 1;
        }).thenApply(x -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            System.out.println("c: " + Thread.currentThread().getName());
            return x + 1;
        }).thenAccept(x -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            System.out.println("d: " + Thread.currentThread().getName());
            System.out.println("result = " + x);
        });

        CompletableFuture<Void> runCompletableFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            System.out.println("a1: " + Thread.currentThread().getName());
           /* LockSupport.parkNanos(1); // 模拟耗时操作*/
            System.out.println("time = " + (System.nanoTime() - start) + "ns");
            return 0;
        }, executor).thenRunAsync(() -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            System.out.println("b1: " + Thread.currentThread().getName());
        }, executor).thenRun(() -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            System.out.println("c1: " + Thread.currentThread().getName());
        }).thenRun(() -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            System.out.println("d1: " + Thread.currentThread().getName());
        });


        runCompletableFuture.get();
        applyCompletableFuture.get();
        System.out.println("end: " + Thread.currentThread().getName());
    }
}