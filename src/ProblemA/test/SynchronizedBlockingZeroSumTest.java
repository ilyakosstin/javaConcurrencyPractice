package ProblemA.test;

import ProblemA.main.BlockingQueue;
import ProblemA.main.SynchronizedBlockingQueue;

public class SynchronizedBlockingZeroSumTest extends AbstractZeroSumTest {

    @Override
    protected BlockingQueue<Integer> provideQueue(int capacity) {
        return new SynchronizedBlockingQueue<>(capacity);
    }
    
}
