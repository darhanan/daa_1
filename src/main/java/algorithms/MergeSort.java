package algorithms;

import metrics.Metrics;

/**
 * Top-down MergeSort with a single reusable buffer.
 *
 * Design points required by the assignment:
 *   - the helper array is allocated exactly once in {@link #sort(int[], Metrics)}
 *     and passed down the recursion, so no {@code new int[...]} happens inside
 *     a recursive call or inside merge();
 *   - subarrays of {@value #CUTOFF} elements or fewer go to Insertion Sort;
 *   - merge() is a single linear pass, so merging costs O(n).
 *
 * Recurrence: T(n) = 2*T(n/2) + O(n)  ->  Master Theorem case 2  ->  O(n log n)
 * in the best, average and worst case.
 */
public final class MergeSort {

    /** Subarrays with at most this many elements are sorted by Insertion Sort. */
    public static final int CUTOFF = 15;

    private MergeSort() {
    }

    /**
     * Sorts {@code a} ascending.
     *
     * Allocates the one and only buffer, then hands it to the recursion.
     */
    public static void sort(int[] a, Metrics m) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        int[] buffer = new int[a.length];
        m.addAllocation();
        sortRange(a, buffer, 0, a.length, m);
    }

    /** Sorts {@code a[lo, hi)} using the shared {@code buffer}. */
    private static void sortRange(int[] a, int[] buffer, int lo, int hi, Metrics m) {
        m.enterRecursion();
        try {
            int n = hi - lo;
            if (n <= CUTOFF) {
                InsertionSort.sort(a, lo, hi, m);
                return;
            }
            int mid = lo + (hi - lo) / 2;
            sortRange(a, buffer, lo, mid, m);
            sortRange(a, buffer, mid, hi, m);

            // Halves are already in order end to end - skip the merge entirely.
            m.addComparison();
            if (a[mid - 1] <= a[mid]) {
                return;
            }
            merge(a, buffer, lo, mid, hi, m);
        } finally {
            m.exitRecursion();
        }
    }

    /**
     * Merges the sorted runs {@code a[lo, mid)} and {@code a[mid, hi)} in O(n).
     *
     * The left half is copied into the shared buffer; the right half stays in
     * place, which halves the copying compared with buffering both sides.
     */
    private static void merge(int[] a, int[] buffer, int lo, int mid, int hi, Metrics m) {
        int leftLen = mid - lo;
        System.arraycopy(a, lo, buffer, lo, leftLen);

        int i = lo;      // read cursor in the buffered left half
        int j = mid;     // read cursor in the right half
        int k = lo;      // write cursor in a

        while (i < mid && j < hi) {
            m.addComparison();
            if (buffer[i] <= a[j]) {
                a[k++] = buffer[i++];
            } else {
                a[k++] = a[j++];
            }
        }
        // Left leftovers still need copying; right leftovers are already in place.
        while (i < mid) {
            a[k++] = buffer[i++];
        }
    }
}
