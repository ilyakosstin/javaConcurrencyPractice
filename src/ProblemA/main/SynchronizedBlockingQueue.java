package ProblemA.main;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

public class SynchronizedBlockingQueue<T> implements BlockingQueue<T> {
    private Queue<T> buffer;
    private int capacity;

    public SynchronizedBlockingQueue(int capacity) {
        buffer = new LinkedList<>();
        this.capacity = capacity;
    }

    @Override
    public void put(T element) throws InterruptedException {
        synchronized(buffer) {
            while(buffer.size() == capacity) {
                buffer.wait();
            }
            buffer.add(element);
            buffer.notifyAll();
        }
    }

    @Override
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

    @Override
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


    @Override
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

    @Override
    public synchronized int size() {
        return buffer.size();
    }

    @Override
    public void waitUntilEmpty() throws InterruptedException {
        synchronized(buffer) {
            while(!buffer.isEmpty()) {
                buffer.wait();
            }
        }
    }

    @Override
    public void clear() {
        synchronized(buffer) {
            buffer.clear();
            buffer.notifyAll();
        }
    }
}