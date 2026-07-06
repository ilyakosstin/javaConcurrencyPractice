package ProblemA.test;

import ProblemA.main.BlockingQueue;
import ProblemA.main.LockBlockingQueue;

public class LockBlockingQueueZeroSumTest extends AbstractZeroSumTest {

    @Override
    protected BlockingQueue<Integer> provideQueue(int capacity) {
        return new LockBlockingQueue<>(capacity, false);
    }
    
}
