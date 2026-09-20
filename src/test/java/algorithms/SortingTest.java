package algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import metrics.Metrics;

/** Correctness and edge-case tests for MergeSort, QuickSort and Insertion Sort. */
class SortingTest {

    private static final int RANDOM_TRIALS = 100;

    @Test
    @DisplayName("MergeSort matches Arrays.sort on 100 random arrays")
    void mergeSortMatchesLibrarySort() {
        Random rnd = new Random(2024);
        for (int trial = 0; trial < RANDOM_TRIALS; trial++) {
            int[] actual = randomArray(rnd, rnd.nextInt(500));
            int[] expected = actual.clone();
            Arrays.sort(expected);

            MergeSort.sort(actual, new Metrics());
            assertArrayEquals(expected, actual, "trial " + trial);
        }
    }

    @Test
    @DisplayName("QuickSort matches Arrays.sort on 100 random arrays")
    void quickSortMatchesLibrarySort() {
        Random rnd = new Random(2025);
        for (int trial = 0; trial < RANDOM_TRIALS; trial++) {
            int[] actual = randomArray(rnd, rnd.nextInt(500));
            int[] expected = actual.clone();
            Arrays.sort(expected);

            QuickSort.sort(actual, new Metrics(), new Random(trial));
            assertArrayEquals(expected, actual, "trial " + trial);
        }
    }

    @Test
    @DisplayName("Insertion Sort matches Arrays.sort on 100 random arrays")
    void insertionSortMatchesLibrarySort() {
        Random rnd = new Random(2026);
        for (int trial = 0; trial < RANDOM_TRIALS; trial++) {
            int[] actual = randomArray(rnd, rnd.nextInt(200));
            int[] expected = actual.clone();
            Arrays.sort(expected);

            InsertionSort.sort(actual, new Metrics());
            assertArrayEquals(expected, actual, "trial " + trial);
        }
    }

    @Test
    @DisplayName("Edge cases: empty, single element, all equal, already sorted, reversed")
    void edgeCases() {
        int[][] cases = {
                {},
                { 42 },
                { 7, 7, 7, 7, 7, 7, 7, 7 },
                { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
                { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 },
                { Integer.MIN_VALUE, Integer.MAX_VALUE, 0 },
        };

        for (int[] original : cases) {
            int[] expected = original.clone();
            Arrays.sort(expected);

            int[] merge = original.clone();
            MergeSort.sort(merge, new Metrics());
            assertArrayEquals(expected, merge, "MergeSort on " + Arrays.toString(original));

            int[] quick = original.clone();
            QuickSort.sort(quick, new Metrics(), new Random(1));
            assertArrayEquals(expected, quick, "QuickSort on " + Arrays.toString(original));

            int[] insertion = original.clone();
            InsertionSort.sort(insertion, new Metrics());
            assertArrayEquals(expected, insertion, "InsertionSort on " + Arrays.toString(original));
        }
    }

    @Test
    @DisplayName("Sizes around the cutoff boundary are handled correctly")
    void cutoffBoundary() {
        Random rnd = new Random(99);
        for (int n = 0; n <= 40; n++) {
            int[] original = randomArray(rnd, n);
            int[] expected = original.clone();
            Arrays.sort(expected);

            int[] merge = original.clone();
            MergeSort.sort(merge, new Metrics());
            assertArrayEquals(expected, merge, "MergeSort at n=" + n);

            int[] quick = original.clone();
            QuickSort.sort(quick, new Metrics(), new Random(n));
            assertArrayEquals(expected, quick, "QuickSort at n=" + n);
        }
    }

    @Test
    @DisplayName("MergeSort allocates its helper buffer exactly once")
    void mergeSortAllocatesOneBuffer() {
        Metrics m = new Metrics();
        MergeSort.sort(randomArray(new Random(5), 100_000), m);
        assertEquals(1, m.getAllocations(),
                "MergeSort must allocate exactly one buffer for the whole sort");
    }

    @Test
    @DisplayName("QuickSort depth on a sorted array of 100 000 stays <= 2*log2(n)")
    void quickSortDepthIsBounded() {
        int n = 100_000;
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i;
        }

        Metrics m = new Metrics();
        QuickSort.sort(sorted, m, new Random(7));

        int limit = (int) (2 * (Math.log(n) / Math.log(2)));
        assertTrue(m.getMaxDepth() <= limit,
                "maxDepth " + m.getMaxDepth() + " must be <= " + limit);

        for (int i = 0; i < n; i++) {
            assertEquals(i, sorted[i], "array must still be correctly sorted");
        }
    }

    @Test
    @DisplayName("QuickSort stays shallow on an all-equal array of 100 000")
    void quickSortDepthOnDuplicates() {
        int n = 100_000;
        int[] equal = new int[n];
        Arrays.fill(equal, 5);

        Metrics m = new Metrics();
        QuickSort.sort(equal, m, new Random(7));

        int limit = (int) (2 * (Math.log(n) / Math.log(2)));
        assertTrue(m.getMaxDepth() <= limit,
                "3-way partition should collapse equal values, depth was " + m.getMaxDepth());
        // One partition pass is enough when every value is equal.
        assertTrue(m.getComparisons() <= 4L * n,
                "all-equal input must stay linear, comparisons=" + m.getComparisons());
    }

    @Test
    @DisplayName("Large sorted and duplicate inputs do not overflow the stack")
    void largeInputsDoNotOverflow() {
        int n = 1_000_000;

        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i;
        }
        QuickSort.sort(sorted, new Metrics(), new Random(3));
        assertEquals(0, sorted[0]);
        assertEquals(n - 1, sorted[n - 1]);

        int[] dup = new int[n];
        Random rnd = new Random(4);
        for (int i = 0; i < n; i++) {
            dup[i] = rnd.nextInt(10);
        }
        int[] expected = dup.clone();
        Arrays.sort(expected);
        QuickSort.sort(dup, new Metrics(), new Random(4));
        assertArrayEquals(expected, dup);
    }

    private static int[] randomArray(Random rnd, int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            // Mix wide-range and small-range values so duplicates appear too.
            a[i] = rnd.nextBoolean() ? rnd.nextInt() : rnd.nextInt(10);
        }
        return a;
    }
}
