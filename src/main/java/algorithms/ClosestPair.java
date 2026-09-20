package algorithms;

import java.util.Arrays;
import java.util.Comparator;

import metrics.Metrics;

/**
 * Bonus Task B - Closest Pair of Points in 2D, O(n log n).
 *
 * Sort by x once, then recursively solve both halves, take d = min(dLeft, dRight)
 * and scan the strip of width 2d around the dividing line in y order. A point in
 * the strip only has to be checked against the next 7 points: the 2d-by-d
 * rectangle ahead of it can hold at most 8 points that are pairwise >= d apart,
 * so the strip scan is linear.
 *
 * Recurrence: T(n) = 2*T(n/2) + O(n) -> Master Theorem case 2 -> O(n log n).
 * The O(n) merge step is achieved by keeping the points y-sorted on the way up
 * (a merge exactly like MergeSort's), instead of re-sorting the strip.
 */
public final class ClosestPair {

    /** An immutable 2D point. */
    public static final class Point {
        public final double x;
        public final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private ClosestPair() {
    }

    /**
     * Returns the smallest distance between any two of the given points.
     *
     * @throws IllegalArgumentException if fewer than two points are supplied
     */
    public static double closestDistance(Point[] points, Metrics m) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("at least two points are required");
        }
        Point[] byX = points.clone();
        m.addAllocation();
        Arrays.sort(byX, Comparator.comparingDouble(p -> p.x));

        Point[] byY = new Point[byX.length];
        m.addAllocation();
        Point[] buffer = new Point[byX.length];
        m.addAllocation();

        return solve(byX, byY, buffer, 0, byX.length - 1, m);
    }

    /** Brute-force reference implementation, O(n^2), used to verify the fast one. */
    public static double bruteForce(Point[] points, Metrics m) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("at least two points are required");
        }
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                m.addComparison();
                best = Math.min(best, distance(points[i], points[j]));
            }
        }
        return best;
    }

    /**
     * Solves the inclusive range {@code byX[lo..hi]} and leaves those points
     * y-sorted in {@code byY[lo..hi]}.
     */
    private static double solve(Point[] byX, Point[] byY, Point[] buffer, int lo, int hi, Metrics m) {
        m.enterRecursion();
        try {
            int n = hi - lo + 1;
            if (n <= 3) {
                return smallCase(byX, byY, lo, hi, m);
            }

            int mid = lo + (hi - lo) / 2;
            double midX = byX[mid].x;

            double dLeft = solve(byX, byY, buffer, lo, mid, m);
            double dRight = solve(byX, byY, buffer, mid + 1, hi, m);
            double d = Math.min(dLeft, dRight);

            mergeByY(byY, buffer, lo, mid, hi, m);

            // Collect the strip in y order - it is a subsequence of the merged run.
            int stripSize = 0;
            for (int i = lo; i <= hi; i++) {
                if (Math.abs(byY[i].x - midX) < d) {
                    buffer[stripSize++] = byY[i];
                }
            }

            // Each point needs at most the next 7 strip neighbours.
            for (int i = 0; i < stripSize; i++) {
                int limit = Math.min(i + 7, stripSize - 1);
                for (int j = i + 1; j <= limit; j++) {
                    m.addComparison();
                    if (buffer[j].y - buffer[i].y >= d) {
                        break;
                    }
                    d = Math.min(d, distance(buffer[i], buffer[j]));
                }
            }
            return d;
        } finally {
            m.exitRecursion();
        }
    }

    /** Handles 2 or 3 points directly and y-sorts them into byY. */
    private static double smallCase(Point[] byX, Point[] byY, int lo, int hi, Metrics m) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = lo; i <= hi; i++) {
            for (int j = i + 1; j <= hi; j++) {
                m.addComparison();
                best = Math.min(best, distance(byX[i], byX[j]));
            }
        }
        for (int i = lo; i <= hi; i++) {
            byY[i] = byX[i];
        }
        Arrays.sort(byY, lo, hi + 1, Comparator.comparingDouble(p -> p.y));
        return best;
    }

    /** Linear merge of the two y-sorted halves of byY, exactly like MergeSort. */
    private static void mergeByY(Point[] byY, Point[] buffer, int lo, int mid, int hi, Metrics m) {
        int i = lo;
        int j = mid + 1;
        int k = lo;
        while (i <= mid && j <= hi) {
            m.addComparison();
            buffer[k++] = (byY[i].y <= byY[j].y) ? byY[i++] : byY[j++];
        }
        while (i <= mid) {
            buffer[k++] = byY[i++];
        }
        while (j <= hi) {
            buffer[k++] = byY[j++];
        }
        System.arraycopy(buffer, lo, byY, lo, hi - lo + 1);
    }

    private static double distance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
