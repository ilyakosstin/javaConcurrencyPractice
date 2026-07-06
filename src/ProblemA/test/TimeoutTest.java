package ProblemA.test;

import org.junit.*;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.TimeoutException;

import ProblemA.main.BlockingQueue;

public class TimeoutTest {

/*
JUnit
1) test, where we fill up the queue, and then just fire offer(e, timeout) it should throw timeout exception
2) same, but with poll(timeout)
3) first test, but we take one element out just before the timeout
4) same with poll
*/

    private int capacity = 5;
    private BlockingQueue<Integer> queue = new BlockingQueue<>(capacity);
    private int timeout = 500;

    @BeforeEach
    private void clearQueue() throws InterruptedException {
        queue.clear();
    }

    @Test
    public void offer_timeout_test() throws InterruptedException {
        for(int i = 0; i < capacity; i++) {
            queue.put(i);
        }

        assertThrows(TimeoutException.class, () -> {
            queue.offer(5, timeout);
        });
    }

    @Test
    public void poll_timeout_test() {
        assertThrows(TimeoutException.class, () -> {
            queue.poll(timeout);
        });
    }

    @Test
    public void offer_success_test() throws InterruptedException {
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