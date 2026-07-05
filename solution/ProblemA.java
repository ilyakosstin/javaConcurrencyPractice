package solution;

import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

class FleetFactory {
    public record Fleet(List<BufferTask> agents, CountDownLatch producerLatch) {}

    public static Fleet getFleet(BlockingQueue<Integer> stack, int nConsumers, int nProducers, int nOperPerProducer) {
        CountDownLatch latch = new CountDownLatch(nProducers);
        Stream<BufferTask> producers = IntStream.range(0, nProducers).mapToObj(i -> (BufferTask)(new ProducerTask(stack, latch, nOperPerProducer)));
        Stream<BufferTask> consumers = IntStream.range(0, nConsumers).mapToObj(i -> (BufferTask)(new ConsumerTask(stack)));
        List<BufferTask> agents = Stream.concat(producers, consumers).toList();
        return new Fleet(agents, latch);
    }
}

public class ProblemA {

    private static int nProducers = 6;
    private static int nConsumers = 3;
    private static int operationsPerProducer = 500;
    private static int capacity = 3;

    public static int test() {
        BlockingQueue<Integer> stack = new BlockingQueue<>(capacity);
        FleetFactory.Fleet fleet = FleetFactory.getFleet(stack, nConsumers, nProducers, operationsPerProducer);

        List<Thread> threads = fleet.agents().stream().map(Thread::new).toList();
        
        threads.stream().forEach(t -> t.start());

        try {
            fleet.producerLatch().await();
            stack.waitUntilEmpty();
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

        // check sum

        int totalSum = fleet.agents().stream().mapToInt(agent -> agent.getRecievedSum()).sum();

        return totalSum;
    }

    public static void main(String[] args) {

        for(int i = 1; i <= 20; i++) {
            int ts = test();
            if (ts != 0) {
                System.out.println("Test " + i + ": total sum = " + ts);
            }
        }
        
    }

}