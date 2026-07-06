package ProblemA.test;

import org.junit.*;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.TimeoutException;

import ProblemA.main.BlockingQueue;

public abstract class AbstractTimeoutTest {

/*
JUnit
1) test, where we fill up the queue, and then just fire offer(e, timeout) it should throw timeout exception
2) same, but with poll(timeout)
3) first test, but we take one element out just before the timeout
4) same with poll
*/

    protected abstract BlockingQueue<Integer> provideQueue(int capacity);

    //private BlockingQueue<Integer> queue;
    private int capacity = 5;
    private int timeout = 500;

    private BlockingQueue<Integer> provideQueue() {
        return provideQueue(capacity);
    }

    // @BeforeAll
    // public static void queueInit() {
    //     //queue = provideQueue(capacity);
    //     System.out.println("QUEUE PROVIDED!");
    //     throw new RuntimeException("parent");
    // }

    // @BeforeEach
    // public void clearQueue() throws InterruptedException {
    //     queue.clear();
    // }

    @Test
    public void offer_timeout_test() throws InterruptedException {
        BlockingQueue<Integer> queue = provideQueue();
        
        for(int i = 0; i < capacity; i++) {
            queue.put(i);
        }

        assertThrows(TimeoutException.class, () -> {
            queue.offer(5, timeout);
        });
    }

    @Test
    public void poll_timeout_test() {
        BlockingQueue<Integer> queue = provideQueue();

        assertThrows(TimeoutException.class, () -> {
            queue.poll(timeout);
        });
    }

    @Test
    public void offer_success_test() throws InterruptedException {
        BlockingQueue<Integer> queue = provideQueue();

        for(int i = 0; i < capacity; i++) {
            queue.put(i);
        }

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(timeout / 2);
                queue.take();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();

        assertDoesNotThrow(() -> {
            queue.offer(5, timeout);
        });

        thread.join();
    }

    @Test
    public void poll_success_test() throws InterruptedException {
        BlockingQueue<Integer> queue = provideQueue();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(timeout / 2);
                queue.put(5);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();

        assertDoesNotThrow(() -> {
            queue.poll(timeout);
        });

        thread.join();
    }
}