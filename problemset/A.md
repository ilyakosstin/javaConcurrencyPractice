## 1) Implement a bounded blocking queue (classic producer/consumer)

**Main problem:**  
Write `BoundedBlockingQueue<E>` with a fixed capacity supporting:

- `put(E e)` blocks when full
- `take()` blocks when empty
- `size()` is thread-safe  
  Implement it using `synchronized` + `wait()`/`notifyAll()` (or `notify()` if you can justify correctness).

**Harder modifications:**

- Add timed operations: `offer(E e, long timeout)` / `poll(long timeout)` and make them correct under spurious wakeups.
- Make it interruptible and ensure you don't swallow interrupts (proper `InterruptedException` handling).
- Re-implement the same API using `ReentrantLock` + two `Condition`s (`notFull`, `notEmpty`) and compare behavior/performance.
