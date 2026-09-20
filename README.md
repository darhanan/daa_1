# DAA Assignment 1 — Divide and Conquer & Asymptotic Notations

Java implementations of **MergeSort**, **QuickSort**, **QuickSelect** and
**Insertion Sort**, plus the bonus **Deterministic Select (Median of Medians)**
and **Closest Pair of Points**, instrumented with a `Metrics` class that records
comparisons, maximum recursion depth and time.

**Author:** Taubakabyl Nurlybek
**Report:** [REPORT.md](REPORT.md) — asymptotic bounds, recurrences, plots and discussion.

---

## Project structure

```
src/main/java/
  algorithms/
    MergeSort.java            # reusable buffer, cutoff 15, linear merge
    QuickSort.java            # random pivot, 3-way partition, smaller-side-first
    QuickSelect.java          # one-sided recursion, iterative loop
    InsertionSort.java        # cutoff sort, also used by median-of-medians
    Partition.java            # shared Dijkstra 3-way partition
    DeterministicSelect.java  # BONUS A: median of medians, O(n) worst case
    ClosestPair.java          # BONUS B: O(n log n) divide and conquer
  metrics/
    Metrics.java              # comparisons, max depth, allocations, nanoTime
  cli/
    BenchmarkRunner.java      # runs the suite, writes results.csv
src/test/java/algorithms/     # JUnit 5: 28 tests
docs/plots/                   # time_vs_n.png, depth_vs_n.png, ratio_vs_n.png
results.csv                   # benchmark output
scripts_plot.py               # regenerates the plots from results.csv
```

---

## Build, test and run

### With Maven

```bash
mvn clean package          # build + run all tests
mvn test                   # tests only
mvn exec:java              # run the benchmark, writes results.csv
```

### Without Maven (plain JDK — verified on JDK 25)

```bash
# 1. Build
mkdir -p target/classes
javac -d target/classes $(find src/main/java -name '*.java')

# 2. Test (JUnit console launcher is vendored in lib/)
mkdir -p target/test-classes
javac -cp target/classes:lib/junit-platform-console-standalone.jar \
      -d target/test-classes $(find src/test/java -name '*.java')
java -Xss16m -jar lib/junit-platform-console-standalone.jar execute \
     -cp target/classes:target/test-classes --scan-classpath

# 3. Benchmark
java -Xss16m -Xmx4g -cp target/classes cli.BenchmarkRunner results.csv

# 4. Plots
python3 scripts_plot.py
```

`-Xmx4g` is for the n = 1 000 000 cases; `-Xss16m` gives the large-input tests
headroom (the algorithms themselves stay within O(log n) stack).

---

## Requirements covered

| Requirement | Where |
|---|---|
| MergeSort: one buffer allocated at top level | `MergeSort.sort` — asserted by `mergeSortAllocatesOneBuffer` |
| MergeSort: cutoff ≤ 15 → Insertion Sort | `MergeSort.CUTOFF` |
| MergeSort: linear merge | `MergeSort.merge` — single pass |
| QuickSort: random pivot | `Partition.partition` |
| QuickSort: recurse smaller side, loop larger | `QuickSort.sortRange` |
| QuickSort: 3-way partition for duplicates | `Partition.partition` |
| QuickSelect: reuses the same partition | `QuickSelect.select` |
| QuickSelect: one side only | `QuickSelect.select` |
| QuickSelect: `IllegalArgumentException` on bad input | `QuickSelect.select` |
| Metrics passed in, not global | `metrics.Metrics` |
| n = 10³…10⁶ × {random, sorted, duplicates} | `BenchmarkRunner.SIZES` / `INPUTS` |
| 5 runs, median reported | `BenchmarkRunner.measure` |
| CSV: `algorithm,input,n,time_ms,comparisons,max_depth` | `results.csv` |
| 100+ random arrays vs `Arrays.sort` | `SortingTest` |
| Edge cases: empty, single, all equal, sorted | `SortingTest.edgeCases` |
| Depth ≤ 2·log₂(n) on sorted 100 000 | `SortingTest.quickSortDepthIsBounded` |
| QuickSelect == `sorted[k]`, 100+ arrays | `SelectTest` |
| Bonus A: Median of Medians | `DeterministicSelect` + REPORT §6 |
| Bonus B: Closest Pair | `ClosestPair` + REPORT §7 |

---

## Test summary

28 JUnit 5 tests, all passing:

- `SortingTest` (9) — correctness vs `Arrays.sort`, edge cases, cutoff
  boundaries, single-allocation check, depth bound on sorted and all-equal
  100 000-element arrays, no stack overflow at n = 1 000 000.
- `SelectTest` (8) — both selects vs `sorted[k]` over 100 random arrays and over
  every k of one array, invalid input, linearity checks.
- `ClosestPairTest` (6) — fast vs brute force on 100 random sets, duplicates,
  collinear points, logarithmic depth.
- `MetricsTest` (5) — depth tracking, counters, reset, timer.

---

## Headline results (median of 5, n = 1 000 000)

| Algorithm | random | sorted | duplicates |
|---|---|---|---|
| MergeSort | 68.97 ms | 1.14 ms | 28.76 ms |
| QuickSort | 62.85 ms | 27.42 ms | 9.21 ms |
| QuickSelect | 7.77 ms | 2.65 ms | 7.29 ms |
| DeterministicSelect | 22.45 ms | 7.98 ms | 15.09 ms |

QuickSort `max_depth` on **sorted** input at n = 1 000 000 is **12**, against the
2·log₂(n) ≈ 39 limit — no stack overflow, as required.
