package tech.tryu.wait;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class BlockingQueueTest {

    @Test(timeout = 10100)
    public void emptyTakeTest() throws InterruptedException {
        BlockingQueue blockingQueue = new BlockingQueue(10);
        Thread consumer = new Thread(() -> {
            try {
                blockingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // consumer 因为 queue 为空条件成立，在 take() 方法中进入等待状态
        consumer.start();

        // 主线程休眠
        TimeUnit.SECONDS.sleep(10);

        // 可以中断，并抛出中断异常
        consumer.interrupt();



    }

    /**
     * 一个生产者，二个消费者，一个阻塞队列，当队列满时，生产者进入等待状态，当队列为空时，消费者进入等待状态
     */
    @Test
    public void QueueTest() {
        BlockingQueue queue = new BlockingQueue(5);
        Thread producer = new Thread(() -> {
            while(true){

            }
        });
    }
}