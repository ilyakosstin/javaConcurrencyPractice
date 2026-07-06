package ProblemA.main;

import java.util.concurrent.TimeoutException;

public interface BlockingQueue<T> {

    void put(T element) throws InterruptedException;

    T take() throws InterruptedException;

    void offer(T element, long timeout) throws InterruptedException, TimeoutException;

    T poll(long timeout) throws InterruptedException, TimeoutException;

    int size() throws InterruptedException;

    void waitUntilEmpty() throws InterruptedException;

    void clear() throws InterruptedException;

}