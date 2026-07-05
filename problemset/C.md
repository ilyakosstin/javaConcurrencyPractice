## 3) Simulate bank transfers (deadlocks, lock ordering, atomicity)

**Main problem:**  
Model `Account { id, balance }` and implement:

- `transfer(from, to, amount)` that is thread-safe and preserves invariants:
  - total sum across all accounts never changes
  - no account goes negative (or define policy)
    Run many threads performing random transfers; assert invariants at the end.

Key learning goals: avoiding deadlocks, ensuring atomicity of multi-object operations, and reasoning about lock scope.

**Harder modifications:**

- Make it deadlock-safe using lock ordering (e.g., lock lower `id` first), and prove to yourself why it works.
- Add `tryTransfer(..., timeout)` using `tryLock` with timeout and rollback behavior if acquisition fails.
- Introduce a high-read workload (`getTotalBalance()`, `getBalance(id)`) and optimize using `ReadWriteLock` or `StampedLock` (including optimistic reads) while keeping transfers correct.
