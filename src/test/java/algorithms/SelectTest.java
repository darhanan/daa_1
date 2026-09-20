package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import metrics.Metrics;

/** Tests for randomised QuickSelect and the deterministic Median-of-Medians select. */
class SelectTest {

    private static final int RANDOM_TRIALS = 100;

    @Test
    @DisplayName("QuickSelect equals sorted[k] on 100 random arrays")
    void quickSelectMatchesSortedK() {
        Random rnd = new Random(31337);
        for (int trial = 0; trial < RANDOM_TRIALS; trial++) {
            int n = 1 + rnd.nextInt(500);
            int[] data = randomArray(rnd, n);
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            int k = rnd.nextInt(n);

            int actual = QuickSelect.select(data.clone(), k, new Metrics(), new Random(trial));
            assertEquals(sorted[k], actual, "trial " + trial + ", n=" + n + ", k=" + k);
        }
    }

    @Test
    @DisplayName("DeterministicSelect equals sorted[k] on 100 random arrays")
    void deterministicSelectMatchesSortedK() {
        Random rnd = new Random(4242);
        for (int trial = 0; trial < RANDOM_TRIALS; trial++) {
            int n = 1 + rnd.nextInt(500);
            int[] data = randomArray(rnd, n);
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            int k = rnd.nextInt(n);

            int actual = DeterministicSelect.select(data.clone(), k, new Metrics());
            assertEquals(sorted[k], actual, "trial " + trial + ", n=" + n + ", k=" + k);
        }
    }

    @Test
    @DisplayName("Both selects agree with sorted[k] for every k of one array")
    void everyKIsCorrect() {
        Random rnd = new Random(11);
        int[] data = randomArray(rnd, 300);
        int[] sorted = data.clone();
        Arrays.sort(sorted);

        for (int k = 0; k < data.length; k++) {
            assertEquals(sorted[k], QuickSelect.select(data.clone(), k, new Metrics(), new Random(k)),
                    "QuickSelect at k=" + k);
            assertEquals(sorted[k], DeterministicSelect.select(data.clone(), k, new Metrics()),
                    "DeterministicSelect at k=" + k);
        }
    }

    @Test
    @DisplayName("Selecting from a one-element array returns that element")
    void singleElement() {
        assertEquals(9, QuickSelect.select(new int[] { 9 }, 0, new Metrics()));
        assertEquals(9, DeterministicSelect.select(new int[] { 9 }, 0, new Metrics()));
    }

    @Test
    @DisplayName("All-equal array returns the common value for every k")
    void allEqual() {
        int[] data = new int[50];
        Arrays.fill(data, 3);
        for (int k = 0; k < data.length; k++) {
            assertEquals(3, QuickSelect.select(data.clone(), k, new Metrics(), new Random(k)));
            assertEquals(3, DeterministicSelect.select(data.clone(), k, new Metrics()));
        }
    }

    @Test
    @DisplayName("Empty array and out-of-range k throw IllegalArgumentException")
    void invalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[0], 0, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(null, 0, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[] { 1, 2, 3 }, -1, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[] { 1, 2, 3 }, 3, new Metrics()));

        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelect.select(new int[0], 0, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelect.select(new int[] { 1, 2, 3 }, 5, new Metrics()));
    }

    @Test
    @DisplayName("QuickSelect on a large sorted array stays linear-ish and shallow")
    void quickSelectOnLargeSortedInput() {
        int n = 1_000_000;
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i;
        }

        Metrics m = new Metrics();
        int median = QuickSelect.select(sorted, n / 2, m, new Random(8));

        assertEquals(n / 2, median);
        // The loop is iterative, so no stack growth regardless of input size.
        assertEquals(1, m.getMaxDepth());
        // Expected comparisons are O(n); allow generous slack for pivot luck.
        assertTrue(m.getComparisons() < 40L * n,
                "comparisons=" + m.getComparisons() + " should stay linear in n");
    }

    @Test
    @DisplayName("DeterministicSelect stays linear on adversarial sorted input")
    void deterministicSelectIsLinearOnSortedInput() {
        int n = 200_000;
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i;
        }

        Metrics m = new Metrics();
        assertEquals(n / 2, DeterministicSelect.select(sorted, n / 2, m));
        // Guaranteed O(n): the constant is larger than QuickSelect's but bounded.
        assertTrue(m.getComparisons() < 60L * n,
                "comparisons=" + m.getComparisons() + " should stay linear in n");
    }

    private static int[] randomArray(Random rnd, int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextBoolean() ? rnd.nextInt() : rnd.nextInt(10);
        }
        return a;
    }
}
