package algorithms;

import metrics.Metrics;

/**
 * Bonus Task A - Median of Medians select with guaranteed O(n) worst case.
 *
 * The pivot is chosen, not gambled on: split the range into groups of 5, sort
 * each group with Insertion Sort, move the group medians to the front, then
 * recursively select the median of those medians.
 *
 * That pivot is guaranteed to be greater than at least 3n/10 elements and
 * smaller than at least 3n/10, so each step discards at least 30% of the range:
 *
 *   T(n) <= T(n/5) + T(7n/10) + O(n)
 *
 * Since 1/5 + 7/10 = 9/10 < 1, the series converges and T(n) = O(n) in the
 * worst case. The Master Theorem does not apply here (the two subproblems have
 * different sizes); the bound comes from the substitution method instead.
 *
 * The constant factor is larger than randomised QuickSelect's, which is why
 * QuickSelect usually wins in wall-clock time despite the weaker guarantee.
 */
public final class DeterministicSelect {

    private static final int GROUP = 5;

    private DeterministicSelect() {
    }

    /**
     * Returns the k-th smallest element of {@code a} (0-based), reordering {@code a}.
     *
     * @throws IllegalArgumentException if the array is null/empty or k is out of range
     */
    public static int select(int[] a, int k, Metrics m) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("array must not be null or empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException(
                    "k out of range: k=" + k + ", valid range is [0, " + (a.length - 1) + "]");
        }
        return select(a, 0, a.length - 1, k, m);
    }

    /** Selects the k-th smallest (absolute index) inside the inclusive range [lo, hi]. */
    private static int select(int[] a, int lo, int hi, int k, Metrics m) {
        m.enterRecursion();
        try {
            while (true) {
                if (lo == hi) {
                    return a[lo];
                }
                if (hi - lo + 1 <= GROUP * 2) {
                    InsertionSort.sort(a, lo, hi + 1, m);
                    return a[k];
                }

                int pivot = medianOfMedians(a, lo, hi, m);
                int[] band = partitionAroundValue(a, lo, hi, pivot, m);
                int lt = band[0];
                int gt = band[1];

                if (k < lt) {
                    hi = lt - 1;
                } else if (k > gt) {
                    lo = gt + 1;
                } else {
                    return a[k];
                }
            }
        } finally {
            m.exitRecursion();
        }
    }

    /**
     * Computes the median of the group-of-5 medians of {@code a[lo..hi]}.
     *
     * Each group is sorted in place and its median swapped to the front of the
     * range, so the medians end up packed in a[lo .. lo+numGroups-1] and can be
     * selected recursively without any extra array.
     */
    private static int medianOfMedians(int[] a, int lo, int hi, Metrics m) {
        int n = hi - lo + 1;
        int numGroups = (n + GROUP - 1) / GROUP;

        for (int g = 0; g < numGroups; g++) {
            int gLo = lo + g * GROUP;
            int gHi = Math.min(gLo + GROUP - 1, hi);
            InsertionSort.sort(a, gLo, gHi + 1, m);
            int medianIdx = gLo + (gHi - gLo) / 2;
            Partition.swap(a, lo + g, medianIdx);
        }

        int medianOfMediansIdx = lo + numGroups / 2;
        return select(a, lo, lo + numGroups - 1, medianOfMediansIdx, m);
    }

    /**
     * Dijkstra 3-way partition around an explicit pivot VALUE.
     *
     * @return {@code {lt, gt}}, the bounds of the equal-to-pivot band
     */
    private static int[] partitionAroundValue(int[] a, int lo, int hi, int pivot, Metrics m) {
        int lt = lo;
        int i = lo;
        int gt = hi;

        while (i <= gt) {
            m.addComparison();
            if (a[i] < pivot) {
                Partition.swap(a, i++, lt++);
            } else {
                m.addComparison();
                if (a[i] > pivot) {
                    Partition.swap(a, i, gt--);
                } else {
                    i++;
                }
            }
        }
        return new int[] { lt, gt };
    }
}
