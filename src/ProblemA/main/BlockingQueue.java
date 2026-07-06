package ProblemA.main;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

public class BlockingQueue<T> {
    private Queue<T> buffer;
    private int capacity;

    public BlockingQueue(int capacity) {
        buffer = new LinkedList<>();
        this.capacity = capacity;
    }

    public void put(T element) throws InterruptedException {
        synchronized(buffer) {
            while(buffer.size() == capacity) {
                buffer.wait();
            }
            buffer.add(element);
            buffer.notifyAll();
        }
    }

    public T take() throws InterruptedException {
        synchronized(buffer) {
            while(buffer.isEmpty()) {
                buffer.wait();
            }
            T element = buffer.poll();
            buffer.notifyAll();
            return element;
        }
    }

    private static long waitWithTimeout(Object target, long timeoutMillis) throws TimeoutException, InterruptedException {
        long startTimeNano = System.nanoTime();

        target.wait(timeoutMillis);

        timeoutMillis -= (System.nanoTime() - startTimeNano) / 1_000_000L;

        if(timeoutMillis <= 0) {
            throw new TimeoutException();
        }

        return timeoutMillis;
    }

    public void offer(T element, long timeout) throws InterruptedException, TimeoutException {
        if(timeout == 0) {
            put(element);
            return;
        }

        synchronized(buffer) {
            while(buffer.size() == capacity) {
                timeout = waitWithTimeout(buffer, timeout);
            }
            buffer.add(element);
            buffer.notifyAll();
        }
    }


    public T poll(long timeout) throws InterruptedException, TimeoutException {
        if(timeout == 0) {
            return take();
        }

        synchronized(buffer) {
            while(buffer.isEmpty()) {
                timeout = waitWithTimeout(buffer, timeout);
            }
            T element = buffer.poll();
            buffer.notifyAll();
            return element;
        }
    }

    public synchronized int size() {
        return buffer.size();
    }

    public void waitUntilEmpty() throws InterruptedException {
        synchronized(buffer) {
            while(!buffer.isEmpty()) {
                buffer.wait();
            }
        }
    }

    public void clear() {
        synchronized(buffer) {
            buffer.clear();
            buffer.notifyAll();
        }
    }
}