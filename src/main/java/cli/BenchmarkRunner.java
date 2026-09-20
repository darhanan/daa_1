package cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import algorithms.ClosestPair;
import algorithms.DeterministicSelect;
import algorithms.InsertionSort;
import algorithms.MergeSort;
import algorithms.QuickSelect;
import algorithms.QuickSort;
import metrics.Metrics;

/**
 * Runs every algorithm over every input type and size and writes results.csv.
 *
 * Each case is executed {@value #RUNS} times and the MEDIAN run is reported,
 * because the first runs are dominated by JVM warm-up (interpreter, then JIT).
 * A short untimed warm-up pass runs first for the same reason.
 *
 * Usage: java cli.BenchmarkRunner [outputCsv]
 */
public final class BenchmarkRunner {

    private static final int RUNS = 5;
    private static final int[] SIZES = { 1_000, 10_000, 100_000, 1_000_000 };
    private static final String[] INPUTS = { "random", "sorted", "duplicates" };
    private static final long SEED = 42L;

    /** One measured run: median time plus the counters from that same run. */
    private record Result(double timeMs, long comparisons, int maxDepth) {
    }

    public static void main(String[] args) throws IOException {
        Path out = Paths.get(args.length > 0 ? args[0] : "results.csv");

        System.out.println("Warming up the JVM...");
        warmUp();

        StringBuilder csv = new StringBuilder();
        csv.append("algorithm,input,n,time_ms,comparisons,max_depth\n");

        for (String input : INPUTS) {
            for (int n : SIZES) {
                System.out.printf("Running n=%,d input=%s...%n", n, input);

                append(csv, "MergeSort", input, n, measure("MergeSort", input, n));
                append(csv, "QuickSort", input, n, measure("QuickSort", input, n));
                append(csv, "QuickSelect", input, n, measure("QuickSelect", input, n));
                append(csv, "DeterministicSelect", input, n, measure("DeterministicSelect", input, n));

                // Insertion Sort is quadratic - only the two small sizes are practical.
                if (n <= 10_000) {
                    append(csv, "InsertionSort", input, n, measure("InsertionSort", input, n));
                }
            }
        }

        // Bonus Task B is measured on point sets, not integer arrays.
        for (int n : new int[] { 1_000, 10_000, 100_000 }) {
            System.out.printf("Running ClosestPair n=%,d...%n", n);
            append(csv, "ClosestPair", "random", n, measureClosestPair(n));
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            w.print(csv);
        }
        System.out.println("\nWrote " + out.toAbsolutePath());
    }

    private static void append(StringBuilder csv, String algo, String input, int n, Result r) {
        csv.append(String.format("%s,%s,%d,%.3f,%d,%d%n",
                algo, input, n, r.timeMs(), r.comparisons(), r.maxDepth()));
    }

    /**
     * Runs one case {@value #RUNS} times and returns the median by time.
     *
     * A fresh copy of the input is generated for every run, so no run benefits
     * from the previous one having already sorted the data.
     */
    private static Result measure(String algo, String input, int n) {
        Result[] results = new Result[RUNS];
        for (int run = 0; run < RUNS; run++) {
            int[] data = generate(input, n, SEED + run);
            Metrics m = new Metrics();
            Random rnd = new Random(SEED + run);

            m.startTimer();
            switch (algo) {
                case "MergeSort" -> MergeSort.sort(data, m);
                case "QuickSort" -> QuickSort.sort(data, m, rnd);
                case "QuickSelect" -> QuickSelect.select(data, n / 2, m, rnd);
                case "DeterministicSelect" -> DeterministicSelect.select(data, n / 2, m);
                case "InsertionSort" -> InsertionSort.sort(data, m);
                default -> throw new IllegalArgumentException("unknown algorithm: " + algo);
            }
            m.stopTimer();

            results[run] = new Result(m.getElapsedMillis(), m.getComparisons(), m.getMaxDepth());
        }
        Arrays.sort(results, (x, y) -> Double.compare(x.timeMs(), y.timeMs()));
        return results[RUNS / 2];
    }

    private static Result measureClosestPair(int n) {
        Result[] results = new Result[RUNS];
        for (int run = 0; run < RUNS; run++) {
            Random rnd = new Random(SEED + run);
            ClosestPair.Point[] pts = new ClosestPair.Point[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new ClosestPair.Point(rnd.nextDouble() * 1e6, rnd.nextDouble() * 1e6);
            }
            Metrics m = new Metrics();
            m.startTimer();
            ClosestPair.closestDistance(pts, m);
            m.stopTimer();
            results[run] = new Result(m.getElapsedMillis(), m.getComparisons(), m.getMaxDepth());
        }
        Arrays.sort(results, (x, y) -> Double.compare(x.timeMs(), y.timeMs()));
        return results[RUNS / 2];
    }

    /**
     * Builds one of the three required input types.
     *
     * random     - uniformly random integers
     * sorted     - already ascending (the classic QuickSort trap)
     * duplicates - values 0..9 only (the 3-way partition's reason to exist)
     */
    private static int[] generate(String type, int n, long seed) {
        Random rnd = new Random(seed);
        int[] a = new int[n];
        switch (type) {
            case "random" -> {
                for (int i = 0; i < n; i++) {
                    a[i] = rnd.nextInt();
                }
            }
            case "sorted" -> {
                for (int i = 0; i < n; i++) {
                    a[i] = i;
                }
            }
            case "duplicates" -> {
                for (int i = 0; i < n; i++) {
                    a[i] = rnd.nextInt(10);
                }
            }
            default -> throw new IllegalArgumentException("unknown input type: " + type);
        }
        return a;
    }

    /** Untimed pass that lets the JIT compile the hot paths before measuring. */
    private static void warmUp() {
        for (int i = 0; i < 40; i++) {
            Metrics m = new Metrics();
            Random rnd = new Random(1);
            MergeSort.sort(generate("random", 20_000, i), m);
            QuickSort.sort(generate("random", 20_000, i), m, rnd);
            QuickSelect.select(generate("random", 20_000, i), 10_000, m, rnd);
            DeterministicSelect.select(generate("random", 20_000, i), 10_000, m);
        }
    }
}
