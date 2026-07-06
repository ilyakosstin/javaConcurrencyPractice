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

    public void offer(T element, long timeout) throws InterruptedException, TimeoutException {
        if(timeout == 0) {
            put(element);
            return;
        }

        synchronized(buffer) {
            while(buffer.size() == capacity) {
                long startTime = System.currentTimeMillis();

                buffer.wait(timeout);

                timeout -= System.currentTimeMillis() - startTime;

                if(timeout <= 0) {
                    throw new TimeoutException();
                }
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
                long startTime = System.currentTimeMillis();

                buffer.wait(timeout);

                timeout -= System.currentTimeMillis() - startTime;

                if(timeout <= 0) {
                    throw new TimeoutException();
                }
            }
            T element = buffer.poll();
            buffer.notifyAll();
            return element;
        }
    }

    public int size() {
        synchronized(buffer) {
            return buffer.size();
        }
    }

    public void waitUntilEmpty() throws InterruptedException {
        synchronized(buffer) {
            while(!buffer.isEmpty()) {
                buffer.wait();
            }
        }
    }

    public void clear() throws InterruptedException {
        synchronized(buffer) {
            buffer.clear();
            buffer.notifyAll();
        }
    }
}