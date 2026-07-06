package ProblemA.main;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockBlockingQueue<T> implements BlockingQueue<T> {

    private final Queue<T> buffer;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Condition isEmpty;

    public LockBlockingQueue(int capacity, boolean fair) {
        this.capacity = capacity;
        buffer = new LinkedList<>();
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        isEmpty = lock.newCondition();
    }

    @Override
    public void put(T element) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while(capacity == buffer.size()) {
                notFull.await();
            }
            buffer.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while(buffer.isEmpty()) {
                notEmpty.await();
            }
            T element = buffer.poll();
            notFull.signal();

            if (buffer.isEmpty()) {
                isEmpty.signal();
            }

            return element;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void offer(T element, long timeoutMillis) throws InterruptedException, TimeoutException {
        if(timeoutMillis == 0) {
            put(element);
            return;
        }

        long timeoutNano = timeoutMillis * 1_000_000L;
        long prevTime = System.nanoTime();
        
        if(!lock.tryLock(timeoutNano, TimeUnit.NANOSECONDS)) {
            throw new TimeoutException();
        }

        timeoutNano -= System.nanoTime() - prevTime;

        try {
            while(capacity == buffer.size() && timeoutNano > 0) {
                // full
                prevTime = System.nanoTime();

                notFull.await(timeoutNano, TimeUnit.NANOSECONDS);

                timeoutNano -= System.nanoTime() - prevTime;
            }

            if (capacity == buffer.size() && timeoutNano <= 0) {
                throw new TimeoutException();
            }

            buffer.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll(long timeoutMillis) throws InterruptedException, TimeoutException {
        if(timeoutMillis == 0) {
            return take();
        }

        long timeoutNano = timeoutMillis * 1_000_000L;
        long prevTime = System.nanoTime();
        
        if(!lock.tryLock(timeoutNano, TimeUnit.NANOSECONDS)) {
            throw new TimeoutException();
        }

        timeoutNano -= System.nanoTime() - prevTime;

        try {
            while(buffer.isEmpty() && timeoutNano > 0) {
                // full
                prevTime = System.nanoTime();

                notEmpty.await(timeoutNano, TimeUnit.NANOSECONDS);

                timeoutNano -= System.nanoTime() - prevTime;
            }

            if (buffer.isEmpty() && timeoutNano <= 0) {
                throw new TimeoutException();
            }

            T element = buffer.poll();

            notFull.signal();

            // potential problems with awakening waitUntilEmpty listener and producers
            if (buffer.isEmpty()) {
                isEmpty.signal();
            }

            return element;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() throws InterruptedException {
        lock.lockInterruptibly();
        
        try {
            return buffer.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void waitUntilEmpty() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while(!buffer.isEmpty()) {
                // full
                isEmpty.await();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() throws InterruptedException {
        lock.lockInterruptibly();
        
        try {
            buffer.clear();
            isEmpty.signal();
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
    
}
