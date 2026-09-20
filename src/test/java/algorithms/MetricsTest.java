package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import metrics.Metrics;

/** The counters themselves must be trustworthy, since every claim rests on them. */
class MetricsTest {

    @Test
    @DisplayName("Depth tracking reports the maximum, not the current, depth")
    void depthTracking() {
        Metrics m = new Metrics();
        m.enterRecursion();
        m.enterRecursion();
        m.enterRecursion();
        assertEquals(3, m.getMaxDepth());
        m.exitRecursion();
        m.exitRecursion();
        m.exitRecursion();
        assertEquals(0, m.getCurrentDepth());
        assertEquals(3, m.getMaxDepth(), "max depth must survive unwinding");
    }

    @Test
    @DisplayName("Comparison counters accumulate")
    void comparisonCounting() {
        Metrics m = new Metrics();
        m.addComparison();
        m.addComparisons(9);
        assertEquals(10, m.getComparisons());
    }

    @Test
    @DisplayName("reset() clears every counter")
    void resetClearsEverything() {
        Metrics m = new Metrics();
        m.addComparison();
        m.addAllocation();
        m.enterRecursion();
        m.reset();

        assertEquals(0, m.getComparisons());
        assertEquals(0, m.getAllocations());
        assertEquals(0, m.getMaxDepth());
        assertEquals(0, m.getCurrentDepth());
    }

    @Test
    @DisplayName("Timer records a positive elapsed time")
    void timerWorks() {
        Metrics m = new Metrics();
        m.startTimer();
        long sink = 0;
        for (int i = 0; i < 1_000_000; i++) {
            sink += i;
        }
        m.stopTimer();
        assertTrue(m.getElapsedNanos() > 0, "elapsed=" + m.getElapsedNanos() + " sink=" + sink);
    }

    @Test
    @DisplayName("Sorts leave the algorithm at depth zero when they finish")
    void algorithmsUnwindCompletely() {
        Metrics m = new Metrics();
        MergeSort.sort(new int[] { 9, 4, 7, 1, 3, 8, 2 }, m);
        assertEquals(0, m.getCurrentDepth());
        assertTrue(m.getMaxDepth() > 0);
    }
}
