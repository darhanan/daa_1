package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import algorithms.ClosestPair.Point;
import metrics.Metrics;

/** Bonus Task B: the fast algorithm must agree with brute force. */
class ClosestPairTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("Fast algorithm matches brute force on 100 random point sets")
    void matchesBruteForce() {
        Random rnd = new Random(777);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + rnd.nextInt(150);
            Point[] pts = randomPoints(rnd, n, 1000);

            double fast = ClosestPair.closestDistance(pts, new Metrics());
            double brute = ClosestPair.bruteForce(pts, new Metrics());

            assertEquals(brute, fast, EPS, "trial " + trial + ", n=" + n);
        }
    }

    @Test
    @DisplayName("Duplicate points give distance zero")
    void duplicatePointsGiveZero() {
        Point[] pts = {
                new Point(1, 1), new Point(5, 5), new Point(1, 1), new Point(9, 2),
        };
        assertEquals(0.0, ClosestPair.closestDistance(pts, new Metrics()), EPS);
    }

    @Test
    @DisplayName("Two points return their own distance")
    void twoPoints() {
        Point[] pts = { new Point(0, 0), new Point(3, 4) };
        assertEquals(5.0, ClosestPair.closestDistance(pts, new Metrics()), EPS);
    }

    @Test
    @DisplayName("Collinear points are handled correctly")
    void collinearPoints() {
        Point[] pts = new Point[50];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new Point(i * 2.0, 0);
        }
        assertEquals(2.0, ClosestPair.closestDistance(pts, new Metrics()), EPS);
    }

    @Test
    @DisplayName("Fewer than two points throws IllegalArgumentException")
    void invalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestDistance(new Point[] { new Point(0, 0) }, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestDistance(null, new Metrics()));
    }

    @Test
    @DisplayName("Recursion depth grows like log n on 100 000 points")
    void depthIsLogarithmic() {
        Random rnd = new Random(12);
        Point[] pts = randomPoints(rnd, 100_000, 1e6);

        Metrics m = new Metrics();
        ClosestPair.closestDistance(pts, m);

        int limit = (int) (2 * (Math.log(pts.length) / Math.log(2)));
        assertTrue(m.getMaxDepth() <= limit,
                "maxDepth " + m.getMaxDepth() + " must be <= " + limit);
    }

    private static Point[] randomPoints(Random rnd, int n, double range) {
        Point[] pts = new Point[n];
        for (int i = 0; i < n; i++) {
            pts[i] = new Point(rnd.nextDouble() * range, rnd.nextDouble() * range);
        }
        return pts;
    }
}
