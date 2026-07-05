## 2) Build a thread-safe cache with correct publication + eviction

**Main problem:**  
Implement an LRU cache `LruCache<K,V>` with:

- `get(K)` and `put(K,V)`
- bounded size (evict least-recently-used)
- thread safety and consistent LRU order under concurrent access  
  You can start from `LinkedHashMap` and decide how to synchronize access (e.g., `synchronized`, `ReentrantReadWriteLock`, etc.). Focus on _correctness first_.

**Harder modifications:**

- Add `getOrCompute(K, Function<K,V>)` ensuring the mapping function runs **at most once per key** even under concurrency (look into `ConcurrentHashMap.computeIfAbsent`, per-key locking, or futures-in-cache patterns).
- Add TTL expiration (time-based eviction) without a global lock bottleneck.
- Add async refresh: if entry is stale, return old value immediately but trigger a background recomputation using `CompletableFuture` (avoid stampedes).
