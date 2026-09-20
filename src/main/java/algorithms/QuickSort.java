package algorithms;

import java.util.Random;

import metrics.Metrics;

/**
 * QuickSort that cannot overflow the stack.
 *
 * Three design points work together:
 *   - random pivot, so sorted input is not a worst case;
 *   - 3-way partition, so many equal values collapse in one pass;
 *   - recurse into the SMALLER side and loop on the larger one.
 *
 * The last point is what bounds the stack: the recursive call always covers at
 * most half of the current range, so the depth of actual recursion is at most
 * log2(n) + O(1) regardless of how unlucky the pivots are. The larger side is
 * handled by reassigning lo/hi in the while loop (tail-call elimination by hand).
 *
 * Recurrence with a balanced split: T(n) = 2*T(n/2) + O(n) -> O(n log n)
 * (Master Theorem case 2). A random pivot gives that split on average.
 */
public final class QuickSort {

    /** Ranges of at most this many elements are finished by Insertion Sort. */
    public static final int CUTOFF = 15;

    private QuickSort() {
    }

    /** Sorts {@code a} ascending with a time-seeded random pivot. */
    public static void sort(int[] a, Metrics m) {
        sort(a, m, new Random());
    }

    /** Sorts {@code a} ascending, with an explicit source of randomness. */
    public static void sort(int[] a, Metrics m, Random rnd) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        sortRange(a, 0, a.length - 1, rnd, m);
    }

    /** Sorts the inclusive range {@code a[lo..hi]}. */
    private static void sortRange(int[] a, int lo, int hi, Random rnd, Metrics m) {
        m.enterRecursion();
        try {
            while (lo < hi) {
                if (hi - lo + 1 <= CUTOFF) {
                    InsertionSort.sort(a, lo, hi + 1, m);
                    return;
                }

                Partition.Range r = Partition.partition(a, lo, hi, rnd, m);
                int leftSize = r.lt - lo;        // size of the "< pivot" part
                int rightSize = hi - r.gt;       // size of the "> pivot" part

                // Recurse on the smaller side, loop on the larger one.
                if (leftSize < rightSize) {
                    sortRange(a, lo, r.lt - 1, rnd, m);
                    lo = r.gt + 1;
                } else {
                    sortRange(a, r.gt + 1, hi, rnd, m);
                    hi = r.lt - 1;
                }
            }
        } finally {
            m.exitRecursion();
        }
    }
}
