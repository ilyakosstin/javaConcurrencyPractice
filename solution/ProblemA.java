package solution;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.Builder;

class BlockingQueue<T> {
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
}

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

public class ProblemA {

    public static void main(String[] args) {
        
        for(int i = 0; i < 100; i++) {
            try {
                ZeroSumTestHarness.builder()
                .nConsumers(5)
                .nProducers(4)
                .capacity(30)
                .operationsPerProducer(40)
                .build()
                .test();  
            } catch (TestFailedException e) {
                System.out.println("fuck: " + e.getLocalizedMessage());
                break;
            }

        }

    }

}