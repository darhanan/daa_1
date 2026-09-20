package algorithms;

import java.util.Random;

import metrics.Metrics;

/**
 * Dijkstra 3-way partition shared by QuickSort and QuickSelect.
 *
 * Partitioning {@code a[lo, hi]} around a randomly chosen pivot leaves
 *
 *   a[lo  .. lt-1] < pivot
 *   a[lt  .. gt  ] = pivot
 *   a[gt+1.. hi  ] > pivot
 *
 * Collapsing the whole equal run in one pass is what keeps arrays with few
 * distinct values (the "duplicates" input) linear instead of quadratic.
 */
public final class Partition {

    private Partition() {
    }

    /** Boundaries of the equal-to-pivot band produced by a partition. */
    public static final class Range {
        public final int lt;
        public final int gt;

        Range(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    /**
     * Partitions {@code a[lo, hi]} (both ends inclusive) around a random pivot.
     */
    public static Range partition(int[] a, int lo, int hi, Random rnd, Metrics m) {
        // A random pivot means no fixed input (sorted, reversed, organ-pipe)
        // can reliably force the O(n^2) case.
        int pivotIndex = lo + rnd.nextInt(hi - lo + 1);
        int pivot = a[pivotIndex];

        int lt = lo;
        int i = lo;
        int gt = hi;

        while (i <= gt) {
            m.addComparison();
            if (a[i] < pivot) {
                swap(a, i++, lt++);
            } else {
                m.addComparison();
                if (a[i] > pivot) {
                    swap(a, i, gt--);
                } else {
                    i++;
                }
            }
        }
        return new Range(lt, gt);
    }

    static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
