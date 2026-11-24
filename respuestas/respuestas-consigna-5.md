# Consigna 5 · La estación meteorológica — Respuestas

Este documento tiene las respuestas a todas las preguntas de la consigna 5 y del README general. **No es el informe**: es el material del que sale el informe.

## Cómo se obtuvieron los números

- Se usó la semilla 2026 y las lecturas de `datos/`: 2.000.000 para un día y 10.000.000 para cinco días.
- Se ejecutó `java -Xmx4g -Xss512m … Main todo` desde la carpeta del proyecto. La salida completa está en `resultados/*.csv` y los gráficos en `respuestas/graficos/`.
- Cada algoritmo arranca con 1 corrida contada, que da las comparaciones y los movimientos y verifica con `Medidor` que el resultado quede ordenado y con los mismos elementos. Después hay 1 corrida de calentamiento y se informa **la mediana de 3**. Si la corrida contada supera 60 s, se informa ese único tiempo y ese algoritmo no se agranda más (regla de corte del README).
- **Criterio de conteo**, igual para todos los algoritmos:
  - Una **comparación** es una comparación entre dos claves.
  - Un **movimiento** es un intercambio o un desplazamiento de un elemento a otra posición, como dice el README.
- Las comparaciones y los movimientos son **exactos y reproducibles**: salieron idénticos en todas las corridas.
- **Los tiempos dependen de la máquina y fueron ruidosos.** En algunas mediciones una corrida tardó el doble que la otra, y hubo saltos sin relación con el algoritmo. Por ejemplo, heapsort tardó 96 ms con 500.000 lecturas en una corrida y 29 ms en la otra, con exactamente las mismas operaciones. Por eso:
  - se hicieron **dos corridas completas** (`resultados/corrida-1/` y `resultados/corrida-2/`, con su salida en `salida.txt`);
  - en `resultados/*.csv` y en este documento se informa, para cada medición, **la menor de las dos medianas**. Las columnas `ms_corrida1` y `ms_corrida2` muestran ambas. El ruido solo puede *sumar* tiempo, nunca restarlo, así que la menor es la que mejor refleja el costo del algoritmo;
  - las conclusiones se apoyan primero en las operaciones y después en el tiempo.

**Gráficos** (en `respuestas/graficos/`):

| Archivo | Muestra | Pregunta |
|---|---|---|
| `lecturas-1-dia.png`, `lecturas-5-dias.png` | Lecturas en el orden en que se tomaron | A2 |
| `histograma-1-dia.png` | Cuántas veces aparece cada valor | A2, E15 |
| `tiempo-vs-n.png` | Tiempo en función de n, **todos los algoritmos** (log-log, con el corte de 60 s y las extrapolaciones) | C7–C9, D |
| `comparaciones-vs-n.png`, `movimientos-vs-n.png` | Comparaciones y movimientos en función de n, todos los algoritmos | C7–C9, D |
| `operaciones-candidatos.png` | Comparaciones y movimientos de los candidatos con 2M y 10M | C7, D10–D12 |
| `permutacion-ciura-vs-heapsort.png` | Ciura vs. heapsort con lecturas originales, mezcladas y permutación 0..n−1 | Preguntas obligatorias |

Se regeneran con `python graficos_lecturas.py <carpeta datos> <carpeta salida>` y `python graficos_resultados.py <carpeta resultados> <carpeta salida>` (requieren matplotlib y numpy).

---

## Contrato del pivote (README, «antes de medir nada»)

El `quicksort` de `TClasificador` no se modifica. Nuestra `particion` deja a la izquierda los elementos **menores** que el pivote y a la derecha los **mayores o iguales**. Recorre el rango con dos índices, `izq` y `der`, que no controlan los límites del rango.

### 1. Con su `particion`, ¿qué condición tiene que cumplir el valor del pivote para que las dos partes sean siempre más chicas que el rango original?

**Respuesta:** el pivote tiene que ser **estrictamente mayor que el mínimo del rango**. Dicho de otra forma, en el rango tiene que haber al menos un elemento menor que él.

**Por qué:**
- La parte derecha nunca queda vacía, porque contiene al propio elemento pivote, que es ≥ pivote.
- La parte izquierda solo deja de estar vacía si hay algún elemento < pivote.
- Si las dos partes tienen al menos un elemento, las dos son más chicas que el rango original.

Con nuestra `particion`, la condición también protege los índices: `izq` siempre frena, a más tardar en el pivote, y `der` frena en un elemento menor que el pivote. Si no hay ningún elemento menor, `der` se sale del rango.

### 2. La estrategia «el mayor de los dos primeros elementos distintos», ¿cumple esa condición?

**Respuesta: sí.** El pivote es el mayor de dos valores distintos que están en el rango, así que el otro valor, que también está en el rango, es menor que él. Por lo tanto el pivote es mayor que el mínimo del rango.

Hay un detalle: si el rango es de la forma `a, a, …, a, b` con `b > a`, el método devuelve el índice `j`, que no cumple `x < j`. El `quicksort` base descarta ese pivote y no ordena ese rango, pero eso es correcto, porque `a, …, a, b` con `a < b` ya está ordenado. Si todo el rango es igual, devuelve −1.

**Qué hicimos:** es la estrategia de nuestro `obtenerPivote`. Ordena `[1, 1, 1, 2]` y los 2.000.000 de lecturas sin problemas: 31,3 M comparaciones y 34,7 ms (`Main pivote`).

### 3. ¿Y «la mediana entre el primero, el del medio y el último»? Prueben con `[1, 1, 1, 2]`.

**Respuesta: no cumple.** En `[1, 1, 1, 2]` la mediana de (1, 1, 2) es **1, que es el mínimo del rango**.

**Qué hicimos:** implementamos la estrategia en `ClasificadorMedianaDeTres` y la probamos (`Main pivote`). Con nuestra `particion` aparecen las dos formas de falla posibles:
- Con `[1, 1, 1, 2]`, `der` busca un elemento menor que 1, no lo encuentra y se sale del vector: **`ArrayIndexOutOfBoundsException: Index -1`**.
- Con las 2.000.000 de lecturas: cuando el rango no empieza en la posición 0, `der` no se sale del vector, pero se pasa al rango vecino. La partición devuelve `k = i`, así que la parte izquierda queda vacía y `quicksort(k, j)` vuelve a recibir **el mismo rango**. La recursión no avanza y termina en **`StackOverflowError`**, después de 69 M comparaciones. Es exactamente el caso que describe el README.

### 4. Si alguna no la cumple, ¿cómo la arreglan?

**Respuesta:** si la mediana resulta igual al **menor de los tres**, usamos como pivote **el mayor de los tres**. El menor de los tres es un elemento del rango y el pivote es estrictamente mayor que él, así que el pivote queda mayor que el mínimo del rango. Si los tres valores son iguales, usamos «el mayor de los dos primeros distintos», que devuelve −1 si todo el rango es igual. Como el contrato pide `x < j`, si el valor elegido solo está en la posición `j`, se intercambia con el del medio.

**Qué hicimos:** está implementado en `ClasificadorMedianaDeTres(true)`. Ordena `[1, 1, 1, 2]` y las lecturas de un día con **28,2 M comparaciones, un 10 % menos** que «dos primeros distintos», y en 30,0 ms contra 34,7 ms.

Hay otras dos formas de arreglarlo:
- Una partición en tres partes (menores, iguales y mayores).
- Cambiar la partición a «≤ a la izquierda, > a la derecha». En ese caso la condición se invierte y el pivote tiene que ser menor que el máximo del rango.

---

## Parte A · Modelar

### 1. ¿Qué se ordena y por qué clave? Una vez ordenado el arreglo, ¿en qué posición está el percentil p?

**Respuesta:**
- Se ordena el arreglo de lecturas de temperatura del día: 2.000.000 de `int` en décimas de grado. Cada elemento es una lectura.
- **La clave es el propio valor**, la temperatura. No hay datos asociados a cada lectura: la hora se pierde al ordenar y no hace falta para los percentiles. Por eso la **estabilidad no importa**, porque dos lecturas iguales no se pueden distinguir.
- El percentil p, con el criterio del rango más cercano, está en la posición **⌈p·n/100⌉** contando desde 1, o sea en el índice ⌈p·n/100⌉ − 1 en Java.
  - Con n = 2.000.000: p está en la posición **20.000·p**. p1 está en la 20.000, p50 en la 1.000.000 y p99 en la 1.980.000.
  - Con n = 10.000.000: p está en la posición 100.000·p.

**Restricciones que impone la situación:**
- Memoria adicional O(1), porque no entra un segundo arreglo.
- Nada de recursión.
- Minimizar las operaciones, porque las comparaciones y los movimientos gastan batería.

El peor caso importa porque el equipo corre solo en el campo, pero los datos no son adversariales.

**Qué hicimos:** la fórmula está en `Percentiles.indice(p, n)`.

### 2. Exploren los datos: mínimo, máximo, cuántos valores distintos. Grafiquen las lecturas en orden. ¿Están ordenadas de alguna manera?

**Respuesta** (`Main exploracion`):

| | 1 día (2.000.000) | 5 días (10.000.000) |
|---|---|---|
| Mínimo | 3,6 °C | 3,6 °C |
| Máximo | 24,4 °C | 24,8 °C |
| Valores distintos | **208** (de un rango de 209 décimas) | **213** |
| Repeticiones por valor | 9.615 en promedio, hasta 27.580 | — |

Los gráficos son `graficos/lecturas-1-dia.png`, `graficos/lecturas-5-dias.png` y `graficos/histograma-1-dia.png`.

**¿Están ordenadas?** No del todo, pero tampoco al azar: están **parcialmente ordenadas**.
- **A escala chica parecen al azar.** Entre lecturas consecutivas, el 47,7 % sube, el 47,6 % baja y el 4,7 % se repite. Es el ruido del sensor, de unas décimas.
- **A escala grande siguen el ciclo diario:**
  - De madrugada bajan hasta unos 8 °C, alrededor de las 3 h.
  - Suben de forma casi continua hasta unos 22 °C, a las 14 h.
  - Entra el frente frío, que provoca una caída brusca de unos 4 °C cerca de las 14:30.
  - Después bajan hasta unos 6 °C a las 24 h.
- Más o menos la mitad del vector es una **tendencia ascendente** y la otra mitad es **descendente**. Cada lectura está cerca, en valor, de sus vecinas. Por eso las lecturas están "casi en su lugar" respecto a grupos grandes, aunque no respecto a sus vecinas inmediatas.
- En 5 días el ciclo se repite 5 veces, con el frente a distinta hora cada día.

### 3. Para cada uno de los seis algoritmos, ¿cumple «sin memoria proporcional a n» y «sin recursión»? ¿Cuáles quedan como candidatos? ¿Alguno se podría escribir sin recursión?

**Respuesta:**

| Algoritmo | Memoria adicional | Recursión | ¿Cumple? |
|---|---|---|---|
| Inserción directa | O(1) | no | ✅ |
| Shellsort | O(1) más el vector de incrementos (~17–20 enteros, O(log n)) | no | ✅ |
| Burbuja | O(1) | no | ✅ |
| Selección directa | O(1) | no | ✅ |
| Heapsort | O(1). `DesplazaElemento` es iterativo, como en la diapositiva de clase | no | ✅ |
| Quicksort | O(1) de datos, pero la pila de llamadas crece con la profundidad | **sí** | ❌ |

**Candidatos:** los cinco primeros cumplen las restricciones. Inserción, burbuja y selección son O(n²) y con n = 2·10⁶ hacen del orden de 10¹² operaciones (ver C9), así que en la práctica **los candidatos reales son shellsort y heapsort**.

**¿Alguno se podría escribir sin recursión?** Sí, **quicksort**, con una pila explícita de rangos pendientes. Si siempre se apila la parte más grande y se sigue con la más chica, la pila nunca tiene más de log₂ n entradas. Medimos que, con estos datos, la recursión llega a 19–20 niveles con 2M y 10M lecturas. Una pila de ~20 pares de enteros no es memoria proporcional a n, así que ese quicksort iterativo cumpliría las dos restricciones. Pero hay que reescribirlo, y el que trae el proyecto, que no se modifica, es recursivo. Por eso lo medimos solo como referencia.

---

## Parte B · Predecir

> La versión completa de la hipótesis está en `hipotesis.md`. Está escrita a partir de la teoría de clase y de la descripción del escenario, **sin usar resultados de mediciones**. No se ajustó después de medir. Donde los resultados no coinciden, se dice en la Parte D.

### 4. Para los candidatos que quedaron, ¿qué orden de complejidad tienen? Para shellsort, ¿de qué depende?

**Respuesta:**
- Inserción directa: O(n²) en el peor caso y en el promedio, y O(n) si el vector ya está ordenado (es adaptativo).
- Burbuja: O(n²) siempre.
- Selección: O(n²) comparaciones siempre, pero solo O(n) intercambios.
- Heapsort: **O(n log n)** en todos los casos. Hace unas 2·n·log₂ n comparaciones, porque en cada nivel elige el mayor hijo y lo compara con el padre.
- **Shellsort: depende de la secuencia de incrementos.** En clase se dio un orden de n^1,26 para secuencias razonables, y la regla de que los incrementos no sean múltiplos entre sí, para que las cadenas se mezclen.

Con n = 2·10⁶ se tiene n² ≈ 4·10¹² y n log₂ n ≈ 4,2·10⁷: las cuadráticas hacen unas 100.000 veces más operaciones.

### 5. Al menos cuatro secuencias de incrementos para shellsort: Shell, Hibbard, Knuth y Ciura, con la complejidad que se conoce para cada una.

**Respuesta** (están implementadas en `SecuenciasIncrementos`):

| Secuencia | Incrementos | Complejidad conocida |
|---|---|---|
| Shell (1959) | n/2, n/4, …, 1 | O(n²) en el peor caso. Con n potencia de 2, posiciones pares e impares no se comparan hasta la última pasada |
| Hibbard (1963) | 1, 3, 7, 15, …, 2ᵏ − 1 | O(n^(3/2)) en el peor caso; se conjetura O(n^(5/4)) en el promedio |
| Knuth (1973) | 1, 4, 13, 40, …, (3ᵏ − 1)/2 | O(n^(3/2)) en el peor caso; empíricamente alrededor de n^1,25 |
| Ciura (2001) | 1, 4, 10, 23, 57, 132, 301, 701, 1750 y después ×2,25 | Sin cota teórica: es empírica y es la mejor conocida en la práctica |

Ciura se publicó solo hasta 1750. Para n más grandes la extendimos multiplicando por 2,25, que es la extensión habitual. Con n = 2M quedan 17 incrementos, hasta 1.149.241.

### 6. Hipótesis: ¿qué algoritmo y qué secuencia esperan que haga menos operaciones con estos datos?

**Respuesta (hipótesis):**
- **Shellsort con la secuencia de Ciura le gana a heapsort en comparaciones y en movimientos**, y por lo tanto también en tiempo.
- Esperamos el orden **Ciura ≤ Knuth < Hibbard < Shell**, con la secuencia de Shell **claramente** peor por usar incrementos múltiplos entre sí.
- Esperamos que heapsort haga unas **2·n·log₂ n ≈ 8·10⁷ comparaciones** y unas **n·log₂ n ≈ 4·10⁷ intercambios** con n = 2M, y que el mejor shellsort haga menos de 8·10⁷ comparaciones y menos de la mitad de esos movimientos.
- Con 10M lecturas (5 ciclos diarios) esperamos que la ventaja de shellsort **se achique un poco**.

**Por qué:**
1. Los datos están parcialmente ordenados y shellsort, por basarse en inserción, es adaptativo. Heapsort no lo es: al armar el montículo destruye el orden que había.
2. Hay muchas claves repetidas, y en la inserción la condición `aux < V[j]` es estricta, así que el elemento frena apenas encuentra uno igual.
3. Heapsort hace 2 comparaciones por nivel y cada paso es un intercambio. Shellsort desplaza los elementos.

Quicksort, sin restricciones, debería ser el más rápido. Inserción, burbuja y selección deberían tardar horas con 2M.

---

## Parte C · Medir

### 7. Midan todos los candidatos con 2.000.000 y 10.000.000 de lecturas. Shellsort con cada secuencia. Tiempo, comparaciones y movimientos.

**Qué hicimos:** `Main candidatos` midió shellsort con las 4 secuencias y heapsort sobre una serie que se duplica, de 62.500 a 2.000.000 lecturas de un día, y además con las 10.000.000 lecturas de 5 días. Todo está en `resultados/candidatos.csv`.

**Respuesta**, con 2.000.000 lecturas (1 día):

| Algoritmo | Tiempo (mediana) | Comparaciones | Movimientos |
|---|---|---|---|
| Shellsort Shell | 91,2 ms | 51.362.888 | 21.632.778 |
| Shellsort Hibbard | 77,5 ms | 47.654.595 | 17.196.639 |
| Shellsort Knuth | 69,8 ms | **38.280.565** | 20.118.908 |
| **Shellsort Ciura** | **67,2 ms** | 40.387.601 | **15.361.852** |
| Heapsort | 109,1 ms | 77.378.462 | 39.942.685 |
| *Quicksort (referencia)* | *38,2 ms* | *31.266.213* | *3.149.598* |

Con 10.000.000 lecturas (5 días):

| Algoritmo | Tiempo (mediana) | Comparaciones | Movimientos |
|---|---|---|---|
| Shellsort Shell | 451,3 ms | 282.317.993 | 103.769.921 |
| Shellsort Hibbard | 476,4 ms | 273.719.261 | 100.518.383 |
| Shellsort Knuth | 402,2 ms | **219.025.212** | 118.843.854 |
| **Shellsort Ciura** | **381,2 ms** | 225.309.950 | **84.563.204** |
| Heapsort | 701,9 ms | 433.992.623 | 222.966.832 |
| *Quicksort (referencia)* | *195,1 ms* | *150.690.461* | *16.751.838* |

Crecimiento de Ciura entre 62.500 y 2.000.000, o sea con n multiplicado por 32: las comparaciones se multiplican por 40,6, lo que equivale a n^1,07. Es un crecimiento cercano a n log n.

### 8. Midan también quicksort, como referencia de «lo que se podría hacer sin restricciones».

**Respuesta:** quicksort es el más rápido en todos los tamaños. Con 2M tarda 38,2 ms y hace 31,3 M comparaciones y 3,1 M movimientos. Con 10M tarda 195,1 ms y hace 150,7 M comparaciones y 16,8 M movimientos (filas en cursiva de las tablas).

**No hubo `StackOverflowError`.** Medimos la profundidad máxima de la recursión: **19 niveles con 2M y 20 con 10M**, por debajo de log₂ n ≈ 21–23. Funciona incluso con una pila de 16 KB. La causa: con solo ~210 valores distintos, cada rango termina enseguida en un bloque de valores iguales, y para esos bloques `obtenerPivote` devuelve −1 y la recursión se corta. Igual, el firmware prohíbe la recursión, así que quicksort queda descartado.

### 9. Midan inserción directa y burbuja en una serie de tamaños menores y extrapolen a 2.000.000 de lecturas.

**Qué hicimos:** `Main cuadraticos` midió inserción directa, burbuja y también selección (para tener los seis algoritmos). Usó lecturas de un día con n = 10.000, 20.000, 40.000, …, y se cortó cuando una corrida pasó los 60 s. Extrapolamos de dos formas:
- con el modelo teórico t ∝ n², desde la última medición;
- con el exponente medido entre las dos últimas mediciones.

Los resultados están en `resultados/cuadraticos.csv`.

Tiempos: la menor de las dos corridas, la mediana de 3 en cada una.

| n | Inserción | Burbuja | Selección |
|---|---|---|---|
| 10.000 | 13,4 ms | 45,4 ms | 50,7 ms |
| 20.000 | 47,5 ms | 173,0 ms | 245,7 ms |
| 40.000 | 181,0 ms | 551,8 ms | 952,2 ms |
| 80.000 | 761,1 ms | 2,31 s | 1,55 s |
| 160.000 | 3,15 s | 9,70 s | 14,13 s |
| 320.000 | 12,27 s | 51,24 s | 58,20 s |
| 640.000 | 48,97 s | 3,3 min (1 corrida, corte) | 3,8 min (1 corrida, corte) |
| 1.280.000 | 3,3 min (1 corrida, corte) | — | — |

**Respuesta (extrapolación a 2.000.000 de lecturas):**

| | Inserción | Burbuja | Selección |
|---|---|---|---|
| Comparaciones a 2M | ≈ 8,4·10¹¹ | 2,0·10¹² (= n(n−1)/2) | 2,0·10¹² (= n(n−1)/2) |
| Movimientos a 2M | ≈ 8,4·10¹¹ | ≈ 8,4·10¹¹ | ≈ 2·10⁶ (crece como n, no como n²) |
| Tiempo estimado (t ∝ n²) | **≈ 8,1 min** (desde 1.280.000) | **≈ 32,5 min** (desde 640.000) | **≈ 37 min** (desde 640.000) |
| Exponente medido en los dos últimos tamaños | b = 2,03 | b = 1,96 | b = 1,96 |

Comparados con los 67 ms de Ciura, son **entre 7.000 y 33.000 veces más lentos**. No sirven para el firmware.

**Cómo se hizo la estimación y por qué es confiable:**
- **Fórmula:** t(2M) = t(n_último) · (2.000.000 / n_último)². Por ejemplo, para inserción: 3,33 min × (2 / 1,28)² ≈ 8,1 min.
- **Las comparaciones confirman n² exactamente.** Al duplicar n se multiplican por 4,00 en los tres algoritmos. En burbuja y selección son exactamente n(n−1)/2, sin importar los datos.
- **Los exponentes medidos con el tiempo dan entre 1,96 y 2,03**, o sea t ∝ n², como dice la teoría.
- **El tiempo de una sola corrida fue engañoso.** En la corrida 1, inserción con 640.000 tardó 97,9 s, el doble de lo esperado (exponente aparente b = 3,00). En la corrida 2 tardó 48,97 s, con exactamente las mismas operaciones. Por eso se combinaron las dos corridas, y por eso se extrapola apoyándose en que las operaciones crecen ×4.
- **El orden temporal casi no ayuda a inserción.** Sus movimientos son exactamente las inversiones del vector, que equivalen a 0,84·n²/4: solo un 16 % menos que un vector al azar. La mitad ascendente del día ayuda, pero la mitad descendente está "al revés" y lo compensa.

---

## Parte D · Analizar

### 10. ¿Cuánto cambia el resultado de shellsort según la secuencia de incrementos? ¿Qué secuencia conviene y por qué?

**Respuesta:** cambia entre un 30 % y un 40 % con estos datos, y más con datos al azar.

| 2M lecturas | Mejor | Peor | Diferencia |
|---|---|---|---|
| Comparaciones | Knuth 38,3 M | Shell 51,4 M | +34 % |
| Movimientos | Ciura 15,4 M | Shell 21,6 M | +41 % |
| Tiempo | Ciura 67,2 ms | Shell 91,2 ms | +36 % |

Con 10M las diferencias son parecidas: +29 % en comparaciones, +41 % en movimientos y +25 % en tiempo. Con las lecturas mezcladas al azar la diferencia crece mucho: Shell hace 73,2 M comparaciones y 45,9 M movimientos, contra 43,6 M y 19,9 M de Ciura, es decir +68 % y ×2,3.

**Conviene Ciura:**
- Hace **la menor cantidad de movimientos y tarda menos** en todos los conjuntos de datos.
- Knuth hace alrededor de un 5 % menos de comparaciones con las lecturas ordenadas en el tiempo, pero entre un 31 % y un 40 % más de movimientos. En comparaciones más movimientos gana Ciura: 55,7 M contra 58,4 M con 2M.
- Con datos al azar Ciura gana en todo.

**Por qué:**
- Los incrementos de Ciura **no son múltiplos entre sí** y crecen de a ×2,25. Cada pasada mezcla cadenas distintas y le deja poco trabajo a la siguiente, sobre todo a la última (h = 1).
- La secuencia de Shell va dividiendo por 2: cada incremento es múltiplo del siguiente, así que se repiten comparaciones entre los mismos grupos y llega más desorden al final. Es lo que se advierte en clase.
- Aun así, con estos datos Shell no se acerca a su peor caso O(n²), que necesita entradas con una estructura particular. Es «solo» entre un 30 % y un 40 % peor.

### 11. Comparen su mejor shellsort con heapsort. ¿Cuál gana en comparaciones, movimientos y tiempo? ¿Coincide con su hipótesis? Expliquen la diferencia.

**Respuesta: shellsort Ciura gana en las tres medidas.**

| | Ciura | Heapsort | Heapsort / Ciura |
|---|---|---|---|
| Comparaciones (2M) | 40,4 M | 77,4 M | ×1,92 |
| Movimientos (2M) | 15,4 M | 39,9 M | ×2,60 |
| Tiempo (2M) | 67,2 ms | 109,1 ms | ×1,62 |
| Comparaciones (10M) | 225,3 M | 434,0 M | ×1,93 |
| Movimientos (10M) | 84,6 M | 223,0 M | ×2,64 |
| Tiempo (10M) | 381,2 ms | 701,9 ms | ×1,84 |

**¿Coincide con la hipótesis?**
- **Sí** en que gana Ciura y en las magnitudes. Heapsort hizo 77,4 M comparaciones contra unas 84 M estimadas (2·n·log₂ n), y 39,9 M intercambios contra unos 42 M estimados (n·log₂ n). Ciura quedó por debajo de 8·10⁷ comparaciones y con menos de la mitad de los movimientos de heapsort.
- **No** en tres puntos:
  - En comparaciones, Knuth le gana a Ciura por poco.
  - La ventaja de shellsort con 10M **no se achicó**: se mantuvo, ×1,92 contra ×1,93 en comparaciones, y en tiempo hasta creció.
  - La razón principal de la ventaja no es el orden temporal sino los valores repetidos (punto 2).

**Explicación de la diferencia:**
1. **Constante de heapsort.** En cada nivel del montículo hace 2 comparaciones (elegir el mayor hijo y compararlo con el padre), a lo largo de ~21 niveles. Son ~39 comparaciones por elemento, contra ~20 de Ciura. Además, cada paso de heapsort es un intercambio, mientras que shellsort desplaza.
2. **Adaptatividad.** Heapsort hace prácticamente las mismas ~77,5 M comparaciones con las lecturas reales, mezcladas o con una permutación sin repetidos: no se adapta. Shellsort sí, pero el experimento (ver las preguntas obligatorias) mostró que lo que más aprovecha son los **valores repetidos**, más que el orden temporal:
   - el orden temporal le ahorra un 7 % de comparaciones y un 23 % de movimientos;
   - los repetidos le ahorran un 36 % y un 64 %.

   **Esto corrige la hipótesis**, que ponía el orden temporal como razón principal.
3. **Tiempo.** La relación de tiempos (×1,6–1,8) es menor que la de operaciones (×1,9–2,6), porque el ciclo interno de shellsort tiene más aritmética de índices. Crece con 10M porque heapsort salta a posiciones lejanas (2i), lo que con 40 MB de datos genera más fallos de caché.

### 12. ¿Cuánto se pierde respecto de quicksort por respetar las restricciones del equipo?

**Respuesta:** respetar las restricciones cuesta **aproximadamente el doble de tiempo**, entre un 30 % y un 50 % más de comparaciones y unas **5 veces más movimientos**.

| | Ciura | Quicksort | Ciura / Quicksort |
|---|---|---|---|
| Tiempo (2M / 10M) | 67,2 / 381,2 ms | 38,2 / 195,1 ms | ×1,76 / ×1,95 |
| Comparaciones (2M / 10M) | 40,4 / 225,3 M | 31,3 / 150,7 M | ×1,29 / ×1,50 |
| Movimientos (2M / 10M) | 15,4 / 84,6 M | 3,1 / 16,8 M | ×4,9 / ×5,0 |

**Por qué:** quicksort mueve cada elemento a su lado del pivote con un solo intercambio. Shellsort, en cambio, desplaza elementos de a un incremento por vez. A cambio, shellsort no necesita pila. Con estos datos quicksort solo necesitaría unos 20 niveles, pero el fabricante prohíbe la recursión.

### 13. Usen su ordenamiento para calcular los percentiles 1, 50 y 99 del día. Verifiquen que tienen sentido en grados.

**Qué hicimos:** `Main percentiles` ordena una copia de las lecturas con shellsort Ciura y toma las posiciones ⌈p·n/100⌉. Después lo verifica contra el método de la pregunta 15.

**Respuesta:**

| | Posición (1 día) | 1 día (2M) | 5 días (10M) |
|---|---|---|---|
| p1 | 20.000 | **6,2 °C** | 6,2 °C |
| p50 | 1.000.000 | **13,0 °C** | 13,0 °C |
| p99 | 1.980.000 | **22,3 °C** | 22,6 °C |

Los 99 percentiles del resumen por radio, en décimas: 62 67 70 72 74 75 76 77 78 79 80 81 82 82 83 84 85 85 86 87 88 88 89 90 91 92 92 93 94 95 96 98 99 100 101 103 104 106 108 110 112 113 115 118 120 122 124 126 128 130 132 134 136 138 140 142 145 147 149 151 152 154 156 158 160 162 163 165 166 168 169 171 172 173 174 175 176 178 179 180 181 182 184 185 187 190 193 196 199 202 205 207 209 211 213 215 218 220 223.

**¿Tienen sentido en grados?** Sí:
- Las lecturas van de 3,6 a 24,4 °C, así que los tres percentiles están dentro del rango y en orden.
- **p1 = 6,2 °C** coincide con el final del día (unos 6 °C a las 24 h) y con el extremo frío del ruido nocturno.
- **p99 = 22,3 °C** está apenas encima del máximo promedio de la tarde (unos 22 °C), como corresponde al 1 % más alto con ruido.
- **p50 = 13,0 °C** cae entre el valle de la noche (8 °C) y la meseta de la tarde (17–22 °C). Es razonable: el histograma tiene dos jorobas y la mediana queda en la zona intermedia.
- Los valores de 5 días son casi iguales porque los días se parecen.
- Además, coinciden **los 99 percentiles** con los calculados por conteo (pregunta 15).

---

## Parte E · Recomendar

### 14. ¿Qué algoritmo y qué secuencia de incrementos recomiendan para el firmware?

**Respuesta:** si hay que ordenar, recomendamos **shellsort con la secuencia de Ciura** (1, 4, 10, 23, 57, 132, 301, 701, 1750, extendida ×2,25).

**Parámetros:**
- Los incrementos se pueden guardar como una tabla fija en el firmware: 17 valores para 2M y 19 para 10M.
- Es iterativo, ordena sobre el mismo arreglo y usa memoria O(1).
- Con 2M lecturas hace unos 40 M comparaciones y 15 M movimientos por día. Es el candidato con menos operaciones totales y menos tiempo, tanto para el resumen diario como para el semanal del concentrador.

**Cuándo dejaría de ser la mejor opción:**
- **Si se necesita una garantía de peor caso.** Ciura no tiene una cota teórica demostrada y heapsort garantiza O(n log n). Si los datos pudieran ser adversariales o la batería no admitiera sorpresas, convendría heapsort, que fue ~1,9 veces más caro con estos datos.
- **Si se permitiera la recursión o memoria O(log n)**, quicksort, o mejor un quicksort iterativo con pila acotada, sería unas 2 veces más rápido.
- **Si el objetivo es solo obtener los percentiles y el rango de valores es conocido, no hay que ordenar**: ver la pregunta 15, que es la verdadera recomendación para el firmware.
- Con n chico, del orden de miles, inserción directa sería igual de buena y más simple.

### 15. Las temperaturas tienen pocos valores distintos y un rango conocido (−50,0 a 60,0 °C). ¿Una forma de obtener los percentiles que aproveche eso y no compare elementos entre sí? ¿Cuánta memoria? ¿Respeta las restricciones?

**Respuesta:** sí, por **conteo**. Es la idea de «cuenta por distribución» vista en clase, pero sin el área de salida.
1. Se usa un arreglo de contadores con una posición por cada valor posible: de −500 a 600 décimas, o sea **1.101 contadores**.
2. Por cada lectura se hace `cuenta[lectura + 500]++`. Se puede hacer **en el momento de tomarla**, sin guardar el arreglo de lecturas.
3. Al final del día se recorren los contadores acumulando. El percentil p es el primer valor cuya cuenta acumulada alcanza la posición ⌈p·n/100⌉. Como los percentiles van en orden, alcanza con un solo recorrido de los 1.101 contadores para los 99.

- **No compara elementos entre sí.** Cada lectura solo se usa como índice. El costo es n incrementos más 1.101 sumas, o sea O(n + k) con k = 1.101.
- **Memoria:** 1.101 contadores de 32 bits son **4.404 bytes**, contra 8 MB del arreglo de 2M `int`. No alcanza con 16 bits: un valor llega a repetirse 27.580 veces en un día y, en un día de temperatura constante, podría repetirse 2.000.000 de veces.
- **¿Respeta las restricciones?** Sí, y mejor que cualquier ordenamiento:
  - la memoria no depende de n;
  - no hay recursión;
  - no compara ni mueve lecturas, y ni siquiera necesita guardarlas.
- **Qué hicimos:** está implementado en `Percentiles.porConteo`. Con 2M lecturas tarda **3,0 ms**, contra 67 ms de ordenar con Ciura, unas 20 veces menos. Con 10M tarda 8,5 ms, contra 381 ms. Los 99 percentiles **coinciden** con los del vector ordenado.
- **Límites:** depende de que el rango sea conocido y chico. Una lectura fuera de −50,0…60,0 °C, por ejemplo por una falla del sensor, hay que descartarla o recortarla; nuestro código la rechaza con una excepción. Si se cambiara la resolución a centésimas, harían falta 11.001 contadores (44 KB).

---

## Preguntas que todos tienen que contestar (README)

Para estas preguntas medimos los candidatos con dos variantes de 2.000.000 de datos (`Main permutacion`, `resultados/permutacion.csv`):
- **Lecturas mezcladas:** las mismas lecturas del día mezcladas al azar. Tienen los mismos valores y los mismos repetidos, pero **no** el orden temporal.
- **Permutación 0..n−1:** sin orden temporal y **sin** repetidos. Es la "permutación al azar" clásica.

Datos con n = 2.000.000; tiempos con el mismo criterio de la menor de dos corridas:

| | Lecturas originales | Lecturas mezcladas | Permutación 0..n−1 |
|---|---|---|---|
| Ciura: comparaciones | 40,4 M | 43,6 M | 67,9 M |
| Ciura: movimientos | 15,4 M | 19,9 M | **54,7 M** |
| Ciura: tiempo | 67 ms | 102 ms | 254 ms |
| Heapsort: comparaciones | 77,4 M | 77,6 M | 77,6 M |
| Heapsort: movimientos | 39,9 M | 40,1 M | **40,1 M** |
| Heapsort: tiempo | 109 ms | 145 ms | **229 ms** |
| Quicksort: comparaciones | 31,3 M | 27,7 M | 82,4 M |

El gráfico de este experimento es `graficos/permutacion-ciura-vs-heapsort.png`.

### ¿Por qué no usar heapsort, que siempre es O(n log n)?

**Respuesta:** porque con **estos** datos hace el doble de trabajo que shellsort Ciura: 1,9 veces más comparaciones, 2,6 veces más movimientos y entre 1,6 y 1,8 veces más tiempo, tanto con 2M como con 10M. Su garantía O(n log n) vale para cualquier entrada, pero heapsort **no se adapta a los datos**: hace las mismas ~77,5 M comparaciones con las lecturas reales, mezcladas o con una permutación sin repetidos. Shellsort, en cambio, aprovecha las propiedades de las lecturas. La garantía de heapsort es la razón para preferirlo solo si los datos pudieran ser adversariales (ver la pregunta 14).

### ¿Qué propiedad de estos datos aprovecha el algoritmo que eligieron?

**Respuesta:** sobre todo los **pocos valores distintos**, que se repiten mucho, y en segundo lugar el **orden temporal parcial**.

**Qué hicimos:** separamos los dos efectos con las variantes de arriba.
- **Orden temporal**, de lecturas mezcladas a originales: Ciura baja 7 % las comparaciones y 23 % los movimientos. Ayuda, pero poco.
- **Valores repetidos**, de permutación 0..n−1 a lecturas mezcladas: Ciura baja **36 % las comparaciones y 64 % los movimientos**. Es el efecto principal.

**Por qué:**
- Las pasadas con incrementos grandes dejan cada elemento cerca de su lugar final. En las pasadas con incrementos chicos, cada elemento solo tiene que acomodarse respecto de sus vecinos.
- Con solo ~208 valores distintos, cada valor ocupa unas 9.600 posiciones seguidas en el vector ordenado. Entonces, los vecinos de un elemento que ya está cerca de su lugar suelen tener **la misma clave**.
- Como la inserción usa `aux < V[j]` estricto, el elemento frena apenas encuentra uno igual, y esas últimas pasadas casi no cuestan.
- Con valores distintos, cada pequeño desorden es una inversión real que hay que corregir.
- Heapsort no aprovecha ninguna de las dos propiedades.

Este resultado corrige en parte la hipótesis: allí pusimos el orden temporal como primera razón y los repetidos como segunda, y fue al revés.

### Si los datos fueran una permutación al azar del mismo tamaño, ¿cambiaría su elección?

**Respuesta:** **depende de qué "al azar".**
- **Si fueran las mismas lecturas en otro orden** (mezcladas): **no cambiaría.** Ciura sigue haciendo bastante menos que heapsort: 43,6 M contra 77,6 M comparaciones y 19,9 M contra 40,1 M movimientos.
- **Si fuera una permutación al azar de valores distintos** (0..n−1): **sí, probablemente elegiríamos heapsort.**
  - Ciura sigue haciendo un 12 % menos de comparaciones (67,9 M contra 77,6 M), pero hace un **36 % más de movimientos** (54,7 M contra 40,1 M).
  - En operaciones totales gana heapsort por poco: 117,7 M contra 122,6 M. **En tiempo también gana heapsort**: 229 ms contra 254 ms.
  - A eso se suman sus otras ventajas: garantía O(n log n) en el peor caso y menos movimientos, que en el equipo cuestan batería.

Quicksort también empeora mucho en ese caso: pasa de 27,7 M a 82,4 M comparaciones, porque ya no aparecen los bloques de valores iguales que cortan la recursión.
