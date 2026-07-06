package ProblemA.test;

import ProblemA.main.BlockingQueue;
import ProblemA.main.LockBlockingQueue;

public class LockBlockingQueueTimeoutTest extends AbstractTimeoutTest {

    @Override
    protected BlockingQueue<Integer> provideQueue(int capacity) {
        return new LockBlockingQueue<>(capacity, false);
    }
    
}
