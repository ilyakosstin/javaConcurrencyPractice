## 5) Create an asynchronous pipeline with `CompletableFuture` (composition + backpressure)

**Main problem:**  
Build a mini async pipeline that:

- Takes a list of inputs (e.g., file paths, URLs, or synthetic tasks)
- Stage A: fetch/load data (I/O simulated with sleep is fine)
- Stage B: parse/transform
- Stage C: aggregate results into a final report  
  Use `CompletableFuture` for composition (`thenApply`, `thenCompose`, `allOf`) and use a custom `Executor` (not the common pool) so you control threads.

**Harder modifications:**

- Add bounded concurrency (backpressure): ensure at most `N` tasks run in Stage A at once (use `Semaphore`, bounded queue, or a custom executor).
- Add failure policy: "fail-fast" (cancel remaining tasks on first error) vs "collect all errors" (return a report with successes + failures).
- Add cancellation propagation: cancel the pipeline and ensure underlying tasks stop (cooperate with interruption and `CompletableFuture` cancellation).
