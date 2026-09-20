package metrics;

/**
 * Collects performance counters for a single algorithm run.
 *
 * An instance is passed explicitly into every algorithm (no static/global state),
 * so runs stay independent and tests can inspect the counters afterwards.
 *
 * Depth tracking uses enterRecursion()/exitRecursion() around each recursive
 * call, keeping a running current depth and the maximum ever reached.
 */
public final class Metrics {

    private long comparisons;
    private long allocations;
    private int currentDepth;
    private int maxDepth;
    private long startNanos;
    private long elapsedNanos;

    /** Counts one key-to-key comparison. */
    public void addComparison() {
        comparisons++;
    }

    /** Counts {@code n} key-to-key comparisons at once (used by tight loops). */
    public void addComparisons(long n) {
        comparisons += n;
    }

    /** Counts one array allocation, to prove MergeSort allocates its buffer once. */
    public void addAllocation() {
        allocations++;
    }

    /** Marks entry into a recursive call and updates the maximum depth. */
    public void enterRecursion() {
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    /** Marks the return from a recursive call. */
    public void exitRecursion() {
        currentDepth--;
    }

    /** Starts the nanosecond timer. */
    public void startTimer() {
        startNanos = System.nanoTime();
    }

    /** Stops the timer and stores the elapsed time. */
    public void stopTimer() {
        elapsedNanos = System.nanoTime() - startNanos;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getAllocations() {
        return allocations;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public double getElapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    /** Clears every counter so the same object can be reused for another run. */
    public void reset() {
        comparisons = 0;
        allocations = 0;
        currentDepth = 0;
        maxDepth = 0;
        startNanos = 0;
        elapsedNanos = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{time=%.3f ms, comparisons=%d, maxDepth=%d, allocations=%d}",
                getElapsedMillis(), comparisons, maxDepth, allocations);
    }
}
