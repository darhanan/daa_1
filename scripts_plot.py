"""Generates the three required plots from results.csv into docs/plots/."""
import csv
import math
import os
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

HERE = os.path.dirname(os.path.abspath(__file__))
CSV = os.path.join(HERE, "results.csv")
OUT = os.path.join(HERE, "docs", "plots")
os.makedirs(OUT, exist_ok=True)

rows = []
with open(CSV, newline="", encoding="utf-8") as f:
    for r in csv.DictReader(f):
        rows.append({
            "algorithm": r["algorithm"],
            "input": r["input"],
            "n": int(r["n"]),
            "time_ms": float(r["time_ms"]),
            "comparisons": int(r["comparisons"]),
            "max_depth": int(r["max_depth"]),
        })

SORTS = ["MergeSort", "QuickSort", "InsertionSort"]
SELECTS = ["QuickSelect", "DeterministicSelect"]
INPUTS = ["random", "sorted", "duplicates"]
COLORS = {
    "MergeSort": "#1f77b4",
    "QuickSort": "#d62728",
    "QuickSelect": "#2ca02c",
    "DeterministicSelect": "#9467bd",
    "InsertionSort": "#ff7f0e",
    "ClosestPair": "#8c564b",
}
STYLE = {"random": "-", "sorted": "--", "duplicates": ":"}


def series(algo, inp):
    pts = sorted([r for r in rows if r["algorithm"] == algo and r["input"] == inp],
                 key=lambda r: r["n"])
    return pts


def plot_metric(metric, ylabel, title, filename, algos, logy=True):
    fig, ax = plt.subplots(figsize=(10, 6))
    for algo in algos:
        for inp in INPUTS:
            pts = series(algo, inp)
            if not pts:
                continue
            ax.plot([p["n"] for p in pts], [p[metric] for p in pts],
                    marker="o", markersize=4,
                    color=COLORS[algo], linestyle=STYLE[inp],
                    label=f"{algo} / {inp}")
    ax.set_xscale("log")
    if logy:
        ax.set_yscale("log")
    ax.set_xlabel("n (array size, log scale)")
    ax.set_ylabel(ylabel)
    ax.set_title(title)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=7, ncol=2)
    fig.tight_layout()
    path = os.path.join(OUT, filename)
    fig.savefig(path, dpi=150)
    plt.close(fig)
    print("wrote", path)


# 1. Time vs n
plot_metric("time_ms", "median time (ms, log scale)",
            "Time vs n (median of 5 runs)", "time_vs_n.png",
            SORTS + SELECTS)

# 2. Max recursion depth vs n
fig, ax = plt.subplots(figsize=(10, 6))
for algo in ["MergeSort", "QuickSort", "QuickSelect", "DeterministicSelect"]:
    for inp in INPUTS:
        pts = series(algo, inp)
        if not pts:
            continue
        ax.plot([p["n"] for p in pts], [p["max_depth"] for p in pts],
                marker="o", markersize=4, color=COLORS[algo],
                linestyle=STYLE[inp], label=f"{algo} / {inp}")
ns = sorted({r["n"] for r in rows if r["algorithm"] == "QuickSort"})
ax.plot(ns, [2 * math.log2(n) for n in ns], color="black", linewidth=2,
        alpha=0.6, label="2*log2(n) limit")
ax.plot(ns, [math.log2(n) for n in ns], color="gray", linewidth=1.5,
        alpha=0.6, linestyle="-.", label="log2(n)")
ax.set_xscale("log")
ax.set_xlabel("n (array size, log scale)")
ax.set_ylabel("max recursion depth")
ax.set_title("Max recursion depth vs n")
ax.grid(True, which="both", alpha=0.3)
ax.legend(fontsize=7, ncol=2)
fig.tight_layout()
plt.savefig(os.path.join(OUT, "depth_vs_n.png"), dpi=150)
plt.close(fig)
print("wrote depth_vs_n.png")

# 3. Ratio vs n - comparisons/(n log2 n) for sorts, comparisons/n for selects
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

for algo in ["MergeSort", "QuickSort"]:
    for inp in INPUTS:
        pts = series(algo, inp)
        if not pts:
            continue
        ax1.plot([p["n"] for p in pts],
                 [p["comparisons"] / (p["n"] * math.log2(p["n"])) for p in pts],
                 marker="o", markersize=5, color=COLORS[algo],
                 linestyle=STYLE[inp], label=f"{algo} / {inp}")
ax1.set_xscale("log")
ax1.set_xlabel("n (log scale)")
ax1.set_ylabel("comparisons / (n * log2 n)")
ax1.set_title("Theta check for the sorts\n(flat line = Theta(n log n) confirmed)")
ax1.grid(True, which="both", alpha=0.3)
ax1.legend(fontsize=8)

for algo in SELECTS:
    for inp in INPUTS:
        pts = series(algo, inp)
        if not pts:
            continue
        ax2.plot([p["n"] for p in pts],
                 [p["comparisons"] / p["n"] for p in pts],
                 marker="o", markersize=5, color=COLORS[algo],
                 linestyle=STYLE[inp], label=f"{algo} / {inp}")
ax2.set_xscale("log")
ax2.set_xlabel("n (log scale)")
ax2.set_ylabel("comparisons / n")
ax2.set_title("Theta check for the selects\n(flat line = Theta(n) confirmed)")
ax2.grid(True, which="both", alpha=0.3)
ax2.legend(fontsize=8)

fig.tight_layout()
plt.savefig(os.path.join(OUT, "ratio_vs_n.png"), dpi=150)
plt.close(fig)
print("wrote ratio_vs_n.png")

# Print the ratio table used in the report's Theta section.
print("\n--- ratio table (sorts: comparisons/(n log2 n)) ---")
for algo in ["MergeSort", "QuickSort"]:
    for inp in INPUTS:
        vals = [(p["n"], p["comparisons"] / (p["n"] * math.log2(p["n"])))
                for p in series(algo, inp)]
        print(f"{algo:<12} {inp:<11}", " ".join(f"n={n}:{v:.3f}" for n, v in vals))
print("\n--- ratio table (selects: comparisons/n) ---")
for algo in SELECTS:
    for inp in INPUTS:
        vals = [(p["n"], p["comparisons"] / p["n"]) for p in series(algo, inp)]
        print(f"{algo:<20} {inp:<11}", " ".join(f"n={n}:{v:.2f}" for n, v in vals))
