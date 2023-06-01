package tech.tryu.wait;

/**
 * 阻塞有界队列，采用 syncronized + wait + notify 实现
 * 1. 队列为空时，消费者线程进入等待状态，等待生产者线程生产数据
 * 2. 队列满时，生产者线程进入等待状态，等待消费者线程消费数据
 */
public class BlockingQueue {

    private Object[] queue;
    private int size;
    private int head;
    private int tail;

    public BlockingQueue(int size) {
        this.size = size;
        queue = new Object[size];
    }

    public synchronized void put(Object obj) throws InterruptedException {
        while (tail == size) {
            wait();
        }
        queue[tail++] = obj;
        notifyAll();
    }

    public synchronized Object take() throws InterruptedException {
        while (head == tail) {
            wait();
        }
        Object obj = queue[head++];
        notifyAll();
        return obj;
    }

}
