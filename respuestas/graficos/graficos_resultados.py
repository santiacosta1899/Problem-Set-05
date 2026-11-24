import csv, sys, math
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

res, salida = sys.argv[1], sys.argv[2]

def leer(nombre):
    with open(f"{res}/{nombre}", encoding="utf-8") as f:
        return list(csv.DictReader(f))

cand = leer("candidatos.csv")
cuad = leer("cuadraticos.csv")
perm = leer("permutacion.csv")

COLORES = {
    "Shellsort SHELL": "#9ecae1", "Shellsort HIBBARD": "#4292c6", "Shellsort KNUTH": "#2171b5",
    "Shellsort CIURA": "#08306b", "Heapsort": "#d94801", "Quicksort (referencia)": "#41ab5d",
    "Inserción directa": "#756bb1", "Burbuja": "#e7298a", "Selección directa": "#a6761d",
}
NOMBRES = {
    "Shellsort SHELL": "Shell (inc. Shell)", "Shellsort HIBBARD": "Shell (inc. Hibbard)",
    "Shellsort KNUTH": "Shell (inc. Knuth)", "Shellsort CIURA": "Shell (inc. Ciura)",
    "Heapsort": "Heapsort", "Quicksort (referencia)": "Quicksort (referencia)",
    "Inserción directa": "Inserción directa", "Burbuja": "Burbuja", "Selección directa": "Selección directa",
}
ORDEN = list(COLORES)

def serie(filas, alg, campo, conjunto="1 día"):
    pts = [(int(r["n"]), float(r[campo])) for r in filas
           if r["algoritmo"] == alg and r["conjunto"] == conjunto and r[campo] not in ("", None)]
    return sorted(pts)

def miles(x, _):
    if x >= 1e6:
        return f"{x/1e6:g}M"
    if x >= 1e3:
        return f"{x/1e3:g}k"
    return f"{x:g}"

N_DIA = 2_000_000

def grafico_vs_n(campo, ylabel, archivo, titulo, unidad=1.0, extrapolar=True, referencias=False):
    fig, ax = plt.subplots(figsize=(11, 6.2))
    for alg in ORDEN:
        filas = cand if alg in ("Shellsort SHELL", "Shellsort HIBBARD", "Shellsort KNUTH", "Shellsort CIURA",
                                "Heapsort", "Quicksort (referencia)") else cuad
        pts = serie(filas, alg, campo)
        if not pts:
            continue
        xs, ys = zip(*pts)
        ys = [y * unidad for y in ys]
        estilo = "--" if alg == "Quicksort (referencia)" else "-"
        ax.plot(xs, ys, estilo, marker="o", ms=4, color=COLORES[alg], label=NOMBRES[alg], lw=1.8)
        # 10M (5 días) como marcador hueco
        p5 = serie(filas, alg, campo, "5 días")
        if p5:
            ax.plot([p5[0][0]], [p5[0][1] * unidad], marker="o", ms=7, mfc="white", mec=COLORES[alg], mew=1.8, ls="none")
        # extrapolación de las cuadráticas a 2M con n^2 desde el último punto
        if extrapolar and filas is cuad and xs[-1] < N_DIA:
            xe = [xs[-1], N_DIA]
            exponente = 1 if (campo == "movimientos" and alg == "Selección directa") else 2
            ye = [ys[-1], ys[-1] * (N_DIA / xs[-1]) ** exponente]
            ax.plot(xe, ye, ":", color=COLORES[alg], lw=1.6)
            ax.plot([N_DIA], [ye[-1]], marker="x", ms=8, color=COLORES[alg], ls="none")
    if referencias:
        xs = [10_000, 10_000_000]
        ax.axhline(60_000 * unidad, color="grey", lw=0.8, ls="-.")
        ax.text(11_000, 60_000 * unidad * 1.15, "corte: 60 s", color="grey", fontsize=8)
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.xaxis.set_major_formatter(FuncFormatter(miles))
    ax.set_xlabel("n (cantidad de lecturas, escala logarítmica)")
    ax.set_ylabel(ylabel)
    ax.set_title(titulo)
    ax.grid(alpha=0.3, which="both")
    leyenda = ax.legend(loc="upper left", fontsize=8.5, ncol=2)
    ax.text(0.99, 0.02, "● 1 día   ○ 5 días (10M)   ✕ extrapolado a 2M (línea punteada)",
            transform=ax.transAxes, ha="right", fontsize=8, color="#444")
    fig.tight_layout(); fig.savefig(f"{salida}/{archivo}", dpi=130); plt.close(fig)

grafico_vs_n("mediana_ms", "tiempo (ms, mediana, escala logarítmica)", "tiempo-vs-n.png",
             "Tiempo en función de n — todos los algoritmos (lecturas de 1 día)", referencias=True)
grafico_vs_n("comparaciones", "comparaciones (escala logarítmica)", "comparaciones-vs-n.png",
             "Comparaciones en función de n — todos los algoritmos")
grafico_vs_n("movimientos", "movimientos (escala logarítmica)", "movimientos-vs-n.png",
             "Movimientos en función de n — todos los algoritmos")

# candidatos con 2M y 10M: comparaciones y movimientos (barras)
cands = ["Shellsort SHELL", "Shellsort HIBBARD", "Shellsort KNUTH", "Shellsort CIURA", "Heapsort", "Quicksort (referencia)"]
fig, ejes = plt.subplots(1, 2, figsize=(12, 4.8))
for ax, (conjunto, n) in zip(ejes, [("1 día", 2_000_000), ("5 días", 10_000_000)]):
    comps, movs = [], []
    for alg in cands:
        fila = next(r for r in cand if r["algoritmo"] == alg and r["conjunto"] == conjunto and int(r["n"]) == n)
        comps.append(float(fila["comparaciones"]) / 1e6); movs.append(float(fila["movimientos"]) / 1e6)
    x = range(len(cands)); w = 0.4
    ax.bar([i - w / 2 for i in x], comps, w, label="comparaciones", color="#2171b5")
    ax.bar([i + w / 2 for i in x], movs, w, label="movimientos", color="#fd8d3c")
    ax.set_xticks(list(x)); ax.set_xticklabels([NOMBRES[a].replace(" (inc. ", "\n(").replace(" (referencia)", "\n(ref.)") for a in cands], fontsize=8)
    ax.set_ylabel("millones de operaciones"); ax.set_title(f"{conjunto}, n = " + f"{n:,}".replace(",", "."))
    ax.grid(alpha=0.3, axis="y"); ax.legend(fontsize=8)
fig.suptitle("Operaciones de los candidatos (y quicksort de referencia)")
fig.tight_layout(); fig.savefig(f"{salida}/operaciones-candidatos.png", dpi=130); plt.close(fig)

# experimento de permutación: Ciura vs heapsort en tres conjuntos de 2M
conjuntos = [("1 día", "lecturas originales"), ("1 día mezclado", "lecturas mezcladas"), ("permutación 0..n-1", "permutación 0..n−1")]
def fila(filas, alg, conjunto):
    return next(r for r in filas if r["algoritmo"] == alg and r["conjunto"] == conjunto and int(r["n"]) == N_DIA)
fig, ejes = plt.subplots(1, 2, figsize=(12, 4.6))
for ax, campo, titulo in [(ejes[0], "comparaciones", "Comparaciones"), (ejes[1], "movimientos", "Movimientos")]:
    x = range(len(conjuntos)); w = 0.38
    for k, (alg, color) in enumerate([("Shellsort CIURA", "#08306b"), ("Heapsort", "#d94801")]):
        vals = [float(fila(cand if c == "1 día" else perm, alg, c)[campo]) / 1e6 for c, _ in conjuntos]
        barras = ax.bar([i + (k - 0.5) * w for i in x], vals, w, label=NOMBRES[alg], color=color)
        ax.bar_label(barras, fmt="%.1f", fontsize=8)
    ax.set_xticks(list(x)); ax.set_xticklabels([e for _, e in conjuntos])
    ax.set_ylabel("millones"); ax.set_title(f"{titulo} (n = 2.000.000)"); ax.grid(alpha=0.3, axis="y"); ax.set_ylim(0, ax.get_ylim()[1] * 1.18); ax.legend(fontsize=8, loc="upper left")
fig.suptitle("¿Qué propiedad aprovecha shellsort? Orden temporal vs. valores repetidos")
fig.tight_layout(); fig.savefig(f"{salida}/permutacion-ciura-vs-heapsort.png", dpi=130); plt.close(fig)
print("ok")
