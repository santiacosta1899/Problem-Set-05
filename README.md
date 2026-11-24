# Trabajo final UT05 · Consigna 5 — La estación meteorológica

Algoritmos y Estructuras de Datos · Clasificación.

Una estación meteorológica guarda 2.000.000 de lecturas de temperatura por día y tiene que mandar los percentiles del 1 al 99. El equipo no puede reservar un segundo arreglo, no admite recursión y conviene que haga la menor cantidad posible de operaciones. El trabajo consiste en elegir el algoritmo de ordenación, medirlo y justificar la elección.

## Estructura

| Carpeta / archivo | Contenido |
|---|---|
| `consigna/` | Letra de la consigna 5 y README general del trabajo |
| `datos/` | Lecturas generadas con la semilla 2026: `lecturas-1-dia.txt` (2M) y `lecturas-5-dias.txt` (10M) |
| `src/main/java/ucu/edu/aed/tda/clasificacion/` | Código (ver abajo) |
| `resultados/` | CSV generados por el `Main` (mediciones y muestras de lecturas) |
| `respuestas/respuestas-consigna-5.md` | Respuesta a cada pregunta de la consigna y del README, con los números |
| `respuestas/hipotesis.md` | Hipótesis escrita antes de medir (Parte B) |
| `respuestas/graficos/` | Gráficos de las lecturas y de los resultados |

### Código

| Clase | Qué hace |
|---|---|
| `TClasificador` | Clase abstracta del proyecto base. No se modifica; trae el `quicksort` fijo |
| `Clasificador` | Nuestra implementación: inserción, shellsort, burbuja, partición y pivote de quicksort, selección directa y heapsort, más cuenta por distribución, binsort, bucketsort y radix. Tiene **contadores de comparaciones y movimientos** |
| `SecuenciasIncrementos` | Incrementos de shellsort: Shell, Hibbard, Knuth y Ciura |
| `Percentiles` | Posición del percentil p (⌈p·n/100⌉), percentiles desde un vector ordenado y **por conteo** (pregunta 15) |
| `ClasificadorMedianaDeTres` | Pivote «mediana de tres», sin corregir y corregido (preguntas del contrato del pivote) |
| `Main` | Mide todo y genera `resultados/*.csv` |
| `datos.*` | Paquete dado por la cátedra: generadores, `Medidor`, `ArchivoDatos` |

## Cómo ejecutar

Hay que ejecutar **desde esta carpeta**, para que el programa encuentre `datos/` y escriba en `resultados/`. Usar siempre `-Xmx4g -Xss512m`.

**Desde VS Code (lo más simple):** en Run and Debug están las configuraciones de `.vscode/launch.json`: *todo*, *candidatos rápido*, *contrato del pivote* y *elegir parte*. Ya traen `-Xmx4g -Xss512m`.

**Desde la terminal (PowerShell)**, con Maven:

```
mvn compile
java -Xmx4g -Xss512m -cp target/classes ucu.edu.aed.tda.clasificacion.Main [parte] [rapido]
```

o, sin Maven, con el JDK 17 o superior:

```
javac -encoding UTF-8 --release 17 -d target/classes (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
java -Xmx4g -Xss512m -cp target/classes ucu.edu.aed.tda.clasificacion.Main [parte] [rapido]
```

| Parte | Qué hace | Duración aproximada |
|---|---|---|
| `pivote` | Contrato del pivote: `[1, 1, 1, 2]` y lecturas de un día con las 3 estrategias | segundos |
| `exploracion` | Parte A: mínimo, máximo, valores distintos, forma del día y muestra para graficar | segundos |
| `candidatos` | Parte C 7–8: shellsort ×4 secuencias, heapsort y quicksort, de 62.500 a 2M y con 10M | ~1 min |
| `permutacion` | Lecturas mezcladas y permutación 0..n−1 (preguntas obligatorias) | ~30 s |
| `cuadraticos` | Parte C 9: inserción, burbuja y selección en una serie que se duplica, con corte a los 60 s y extrapolación | ~25 min |
| `percentiles` | Partes D 13 y E 15: percentiles con shellsort Ciura y por conteo | segundos |
| `todo` (por defecto) | Todo lo anterior | ~30 min |

`rapido` usa tamaños 10 veces más chicos y un corte de 5 s. Sirve para probar o para la corrida en vivo, y sus CSV llevan el sufijo `-rapido`.

### Cómo mide

Para cada algoritmo:
1. Hace 1 corrida contada, que da comparaciones y movimientos y verifica con `Medidor` que el vector quede ordenado y con los mismos elementos.
2. Hace 1 corrida de calentamiento y se queda con la **mediana de 3**.
3. Si la corrida contada supera 60 s, informa ese único tiempo y no agranda más ese algoritmo.

El `Main` corre en un hilo con 512 MB de pila, lo mismo que `-Xss512m`.
