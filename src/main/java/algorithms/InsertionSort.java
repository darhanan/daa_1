package algorithms;

import metrics.Metrics;

/**
 * Insertion Sort over a half-open range [lo, hi).
 *
 * Used as the small-array cutoff inside MergeSort and QuickSort, and to sort
 * groups of five in the deterministic Median-of-Medians select.
 *
 * Best case O(n) on an already sorted range (one failing comparison per
 * element), worst and average case O(n^2).
 */
public final class InsertionSort {

    private InsertionSort() {
    }

    /** Sorts the whole array. */
    public static void sort(int[] a, Metrics m) {
        sort(a, 0, a.length, m);
    }

    /**
     * Sorts {@code a[lo, hi)} in place.
     *
     * The inner loop shifts larger elements one slot to the right instead of
     * swapping, so each iteration costs one comparison and one move.
     */
    public static void sort(int[] a, int lo, int hi, Metrics m) {
        for (int i = lo + 1; i < hi; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= lo) {
                m.addComparison();
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }
}
