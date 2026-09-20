package algorithms;

import java.util.Random;

import metrics.Metrics;

/**
 * Randomised QuickSelect: returns the k-th smallest element, k counted from 0.
 *
 * It reuses {@link Partition} and then continues in ONE side only - the side
 * that contains position k. Because the equal band is already in final position,
 * a k that lands inside it finishes immediately.
 *
 * Recurrence with a balanced split: T(n) = T(n/2) + O(n).
 * Here a = 1, b = 2, f(n) = n, and n^(log_b a) = n^0 = 1, so f(n) dominates:
 * Master Theorem case 3 gives T(n) = O(n) - a different case from MergeSort's.
 * The worst case is still O(n^2) if pivots are repeatedly extreme.
 *
 * The loop is iterative, so recursion depth stays at 1 and the stack is safe.
 */
public final class QuickSelect {

    private QuickSelect() {
    }

    /** Selects with a time-seeded random pivot. */
    public static int select(int[] a, int k, Metrics m) {
        return select(a, k, m, new Random());
    }

    /**
     * Returns the k-th smallest element of {@code a} (0-based).
     *
     * Note: this reorders {@code a} in place, like the textbook algorithm.
     *
     * @throws IllegalArgumentException if the array is null/empty or k is out of range
     */
    public static int select(int[] a, int k, Metrics m, Random rnd) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("array must not be null or empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException(
                    "k out of range: k=" + k + ", valid range is [0, " + (a.length - 1) + "]");
        }

        int lo = 0;
        int hi = a.length - 1;
        m.enterRecursion();
        try {
            while (true) {
                if (lo == hi) {
                    return a[lo];
                }

                Partition.Range r = Partition.partition(a, lo, hi, rnd, m);

                if (k < r.lt) {
                    hi = r.lt - 1;          // k is in the "< pivot" part
                } else if (k > r.gt) {
                    lo = r.gt + 1;          // k is in the "> pivot" part
                } else {
                    return a[k];            // k landed inside the equal band
                }
            }
        } finally {
            m.exitRecursion();
        }
    }
}
