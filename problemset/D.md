## 4) Build a cancellable job runner with graceful shutdown (volatile + interruption)

**Main problem:**  
Implement a small framework:

- Submit `Runnable`/`Callable` jobs to a fixed thread pool (`ExecutorService`)
- Track job states (`QUEUED/RUNNING/SUCCESS/FAILED/CANCELLED`)
- Support cancellation:
  - `cancel(jobId)` should attempt to stop running tasks and prevent queued tasks from running
- Support graceful shutdown:
  - stop accepting new jobs
  - allow in-flight jobs to finish or be interrupted depending on shutdown mode  
    Use `volatile` for a shutdown flag and correctly combine it with thread interruption.

**Harder modifications:**

- Ensure your worker loop reacts correctly to both `volatile` flags and `Thread.interrupt()` (no busy-waiting, no lost interrupts).
- Add a `Future`-like API (e.g., `JobHandle<T>`) exposing `get()`, `get(timeout)`, `isDone()`, `isCancelled()` implemented using `Lock`/`Condition` or `CountDownLatch`.
- Add metrics (queued count, running count, average runtime) without global contention (consider `LongAdder`).
