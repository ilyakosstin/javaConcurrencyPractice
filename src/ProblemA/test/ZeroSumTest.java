package ProblemA.test;

import ProblemA.main.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.Random;
import lombok.Builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.stream.*;

import org.junit.jupiter.api.RepeatedTest;

interface BufferTask extends Runnable {
    public int getRecievedSum();
}

class ConsumerTask implements BufferTask {
    private int sum;
    private BlockingQueue<Integer> source;

    public ConsumerTask(BlockingQueue<Integer> source) {
        this.source = source;
    }

    @Override
    public int getRecievedSum() {
        return sum;
    }

    @Override
    public void run() {
        try {
            while(true) {
                int x = source.take();
                sum += x;
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }
}

class ProducerTask implements BufferTask {
    private static Random rand = new Random(42);

    private final BlockingQueue<Integer> dest;
    private final CountDownLatch latch;

    private int nOperations;
    private int sum;

    public ProducerTask(BlockingQueue<Integer> dest, CountDownLatch latch, int nOperations) {
        this.dest = dest;
        this.latch = latch;
        this.nOperations = nOperations;
    }

    @Override
    public int getRecievedSum() {
        return -sum;
    }

    @Override
    public void run() {
        try {
            while(nOperations > 0) {
                int x = rand.nextInt(1, 30);
                dest.put(x);
                sum += x;
                nOperations--;
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }
}

class TestFailedException extends Exception {
    public TestFailedException(String message) {
        super(message);
    }
}

@Builder
class ZeroSumTestHarness {

    private final int nProducers;
    private final int nConsumers;
    private final int operationsPerProducer;
    private final int capacity;

    private List<BufferTask> getTasks(BlockingQueue<Integer> target, CountDownLatch producerLatch) {
        Stream<BufferTask> producers = IntStream.range(0, nProducers).mapToObj(i -> (BufferTask)(new ProducerTask(target, producerLatch, operationsPerProducer)));
        Stream<BufferTask> consumers = IntStream.range(0, nConsumers).mapToObj(i -> (BufferTask)(new ConsumerTask(target)));
        return Stream.concat(producers, consumers).toList();
    }

    public void test() throws TestFailedException {
        BlockingQueue<Integer> queue = new BlockingQueue<>(capacity);
        CountDownLatch latch = new CountDownLatch(nProducers);
        List<BufferTask> tasks =  getTasks(queue, latch);
        List<Thread> threads = tasks.stream().map(Thread::new).toList();

        threads.stream().forEach(t -> t.start());

        try {
            latch.await();
            queue.waitUntilEmpty();
        } catch(InterruptedException e) {
            throw new RuntimeException("Waiting was interrupted unexpectidly", e);
        }

        threads.stream().forEach(t -> t.interrupt());
        threads.stream().forEach(t -> {
            try {
                t.join();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        int totalSum = tasks.stream().mapToInt(task -> task.getRecievedSum()).sum();

        if(totalSum != 0) {
            throw new TestFailedException("Total Sum = " + totalSum + " != 0");
        }
    }

}


public class ZeroSumTest {

    static Random rand = new Random();

    @RepeatedTest(value = 30)
    public void test() {

        assertDoesNotThrow(() -> {
            ZeroSumTestHarness.builder()
            .nProducers(rand.nextInt(1, 5))
            .nConsumers(rand.nextInt(1, 5))
            .capacity(rand.nextInt(1, 500))
            .operationsPerProducer(rand.nextInt(1, 1000))
            .build()
            .test();
        });

    }


}