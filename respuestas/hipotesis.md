# Hipótesis (Parte B · preguntas 4, 5 y 6)

> Escrita a partir de la teoría vista en clase (Clasificación Partes 1 y 2) y de la descripción del escenario, **sin usar resultados de mediciones**.

## Candidatos y su orden de complejidad (pregunta 4)

El equipo impone dos restricciones: no se puede reservar memoria proporcional a n y no se permite la recursión. Inserción directa, burbuja, selección directa, shellsort y heapsort ordenan sobre el mismo vector y son iterativos, así que cumplen las dos. Quicksort no las cumple, porque es recursivo. Se podría escribir con una pila explícita, pero esa pila igual ocupa memoria adicional (O(log n) si se procesa primero la parte más chica, O(n) en el peor caso), así que lo dejamos solo como referencia.

| Candidato | Orden (material de clase) |
|---|---|
| Inserción directa | O(n²) en el peor caso y en el promedio; O(n) si el vector ya está ordenado |
| Burbuja | O(n²) siempre |
| Selección directa | O(n²) comparaciones siempre, pero solo O(n) intercambios |
| Shellsort | Depende de la secuencia de incrementos; en clase se da un orden de n^1,26 para secuencias razonables |
| Heapsort | O(n log n) en el peor caso y en el promedio |

Con n = 2.000.000, n² ≈ 4·10¹², mientras que n log₂ n ≈ 4,2·10⁷. Inserción, burbuja y selección hacen unas 100.000 veces más operaciones. Eso las descarta para el firmware, que funciona a batería solar. Los candidatos reales son **shellsort y heapsort**.

El orden de shellsort depende de la secuencia de incrementos. En clase se vio que *los incrementos no deben ser múltiplos de sí mismos, de forma que las cadenas se mezclen entre sí lo más a menudo posible*. Si un incremento es múltiplo del siguiente, la pasada con el incremento menor vuelve a comparar elementos que ya se habían comparado entre sí, y el último paso (h = 1) recibe más trabajo pendiente.

## Secuencias de incrementos (pregunta 5)

| Secuencia | Incrementos | Complejidad conocida |
|---|---|---|
| Shell (1959) | n/2, n/4, …, 1 | O(n²) en el peor caso: con n potencia de 2, los elementos de posiciones pares e impares no se comparan entre sí hasta la última pasada |
| Hibbard (1963) | 1, 3, 7, 15, …, 2ᵏ − 1 | O(n^(3/2)) en el peor caso; se conjetura O(n^(5/4)) en el promedio |
| Knuth (1973) | 1, 4, 13, 40, …, (3ᵏ − 1)/2 | O(n^(3/2)) en el peor caso; empíricamente alrededor de n^1,25 |
| Ciura (2001) | 1, 4, 10, 23, 57, 132, 301, 701, 1750 (extendida con ×2,25) | Sin cota teórica: se obtuvo experimentalmente y es la mejor secuencia conocida en la práctica |

La secuencia original de Shell es justo la que viola la recomendación de clase: cada incremento es el doble del siguiente.

## Hipótesis (pregunta 6)

**Creemos que shellsort con la secuencia de Ciura es el algoritmo que menos operaciones hace con estos datos, y que le gana a heapsort tanto en comparaciones como en movimientos.** Esperamos que el orden de las secuencias, de mejor a peor, sea **Ciura ≤ Knuth < Hibbard < Shell**.

Si solo miramos el orden de complejidad, la ventaja de shellsort no es obvia. Para n = 2·10⁶ se tiene n^1,26 ≈ 9·10⁷ y n log₂ n ≈ 4,2·10⁷, así que por la fórmula sola heapsort parecería competitivo. Nuestra apuesta se basa en las propiedades de **estos** datos y en cuánto cuesta cada operación:

1. **Los datos no están desordenados al azar.** Son lecturas en el orden en que se tomaron. La temperatura sigue el ciclo del día (sube a la mañana y baja a la tarde) con poco ruido, así que valores cercanos en el vector son parecidos. Shellsort se basa en inserción directa, que es **adaptativa**: hace menos trabajo cuanto más ordenada está la entrada. Heapsort no es adaptativo: al armar el montículo destruye el orden que había, y la fase de extracción cuesta lo mismo con cualquier entrada.
2. **Hay muchas claves repetidas**: unas pocas centenas de valores distintos entre 2.000.000 de lecturas. En shellsort, la condición `aux < V[j]` es estricta, así que un elemento se detiene apenas encuentra uno igual. Eso acorta las inserciones. Heapsort no saca provecho de los repetidos.
3. **El costo por operación es menor en shellsort.** Heapsort hace unas 2·log₂ n comparaciones por cada `DesplazaElemento`: elige el mayor hijo y lo compara con el padre en cada nivel. Además, cada paso es un intercambio. Esperamos que haga unas **2·n·log₂ n ≈ 8·10⁷ comparaciones** y unas **n·log₂ n ≈ 4·10⁷ intercambios** con n = 2M. Shellsort mueve los elementos desplazándolos (una asignación por posición) y no intercambiándolos.

Por lo tanto, esperamos que **Ciura y Knuth hagan claramente menos comparaciones que heapsort** (menos de 8·10⁷ con n = 2M) **y menos de la mitad de sus movimientos**. También esperamos que eso se refleje en el tiempo. Ponemos a Ciura por encima de Knuth porque es la mejor secuencia empírica conocida, aunque la diferencia entre las dos debería ser chica. En cambio, esperamos que la secuencia original de Shell sea **claramente** peor que las otras tres, por usar incrementos múltiplos entre sí.

Con 10.000.000 de lecturas (cinco días) esperamos la misma conclusión. El costo debería crecer algo más que linealmente, más de 5 veces al pasar de 2M a 10M. Ahí hay cinco ciclos diarios en vez de uno, así que el vector está menos "ordenado a lo grande" y la ventaja de shellsort podría achicarse un poco.

**Referencias sin restricciones:**
- Quicksort debería ser el más rápido, como O(n log n) en el promedio, con constantes bajas y pocos movimientos.
- Con la estrategia de pivote "el mayor de los dos primeros distintos" y tantos valores repetidos, puede recursar profundo. No descartamos un `StackOverflowError`, ni particiones desbalanceadas que lo acerquen a O(n²).
- Inserción directa, burbuja y selección deberían crecer aproximadamente como n²: al duplicar n, el tiempo se multiplica por 4. Extrapoladas a 2M, deberían tardar horas.

**Qué refutaría la hipótesis:**
- Que heapsort hiciera menos comparaciones que el mejor shellsort.
- Que la secuencia de Shell resultara tan buena como las otras.
- Que el tiempo de shellsort creciera claramente más rápido que n log n entre 2M y 10M.
