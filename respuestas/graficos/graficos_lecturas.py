import sys, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
datos, salida = sys.argv[1], sys.argv[2]
def leer(nombre):
    with open(f"{datos}/{nombre}") as f:
        return np.array(f.read().split(), dtype=np.int32)
for nombre, dias, archivo in [("lecturas-1-dia.txt", 1, "lecturas-1-dia.png"), ("lecturas-5-dias.txt", 5, "lecturas-5-dias.png")]:
    d = leer(nombre); n = len(d); horas = np.arange(n) * 24.0 * dias / n
    paso = max(1, n // 200_000)
    fig, ax = plt.subplots(figsize=(11, 4.2))
    ax.plot(horas[::paso], d[::paso] / 10, ",", color="#9bb7d4", alpha=0.6, label=f"lecturas (1 de cada {paso})")
    bloque = n // (24 * 4 * dias)  # promedio cada 15 min
    prom = d[: bloque * (n // bloque)].reshape(-1, bloque).mean(axis=1) / 10
    ax.plot(np.arange(len(prom)) * bloque * 24.0 * dias / n + 24.0 * dias * bloque / (2 * n), prom, color="#1f4e79", lw=1.6, label="promedio cada 15 min")
    ax.set_xlabel("hora desde el inicio (h)"); ax.set_ylabel("temperatura (°C)")
    ax.set_title("Lecturas en el orden en que se tomaron — " + ("1 día" if dias == 1 else f"{dias} días") + ", n = " + f"{n:,}".replace(",", "."))
    ax.set_xticks(np.arange(0, 24 * dias + 1, 3 if dias == 1 else 12)); ax.grid(alpha=0.3); ax.legend(loc="upper left", markerscale=20)
    fig.tight_layout(); fig.savefig(f"{salida}/{archivo}", dpi=130); plt.close(fig)
    print(nombre, n, d.min(), d.max(), len(np.unique(d)))
d = leer("lecturas-1-dia.txt"); v, c = np.unique(d, return_counts=True)
fig, ax = plt.subplots(figsize=(11, 3.8)); ax.bar(v / 10, c, width=0.1, color="#1f4e79")
ax.set_xlabel("temperatura (°C)"); ax.set_ylabel("cantidad de lecturas"); ax.set_title(f"Cuántas veces aparece cada valor — 1 día ({len(v)} valores distintos)")
ax.grid(alpha=0.3); fig.tight_layout(); fig.savefig(f"{salida}/histograma-1-dia.png", dpi=130)
print("max repeticiones de un valor:", c.max(), "promedio:", round(c.mean()))
