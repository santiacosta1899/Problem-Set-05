package ucu.edu.aed.tda.clasificacion;

import java.util.Arrays;

/**
 * Implementación de TClasificador con contadores de operaciones.
 *
 * Criterio de conteo (el mismo para todos los algoritmos):
 * <ul>
 *   <li><b>Comparación:</b> cada comparación entre dos claves del vector (o entre una clave y
 *   el pivote / el menor provisorio). No se cuentan las comparaciones de índices.</li>
 *   <li><b>Movimiento:</b> cada intercambio de dos elementos cuenta 1, y cada desplazamiento de
 *   un elemento a otra posición cuenta 1 (en inserción y shellsort: cada corrimiento y la
 *   colocación final del elemento, si cambió de lugar). En los métodos de distribución, cada
 *   elemento ubicado en el área de salida cuenta 1.</li>
 * </ul>
 * Antes de medir un algoritmo hay que llamar a {@link #reiniciarContadores()}.
 *
 * Convención de índices: el pseudocódigo de clase usa posiciones 1..N; acá el vector es 0..N-1,
 * salvo en heapsort/desplazaElemento, que siguen las posiciones 1..N de las diapositivas
 * (la posición p del montículo es datos[p - 1]).
 */
public class Clasificador extends TClasificador {

    private long comparaciones;
    private long movimientos;

    // ------------------------------------------------------------------ contadores

    public void reiniciarContadores() {
        comparaciones = 0;
        movimientos = 0;
    }

    public long getComparaciones() {
        return comparaciones;
    }

    public long getMovimientos() {
        return movimientos;
    }

    /** a < b, contando una comparación de claves */
    private boolean menor(int a, int b) {
        comparaciones++;
        return a < b;
    }

    /** intercambio contado como un movimiento */
    @Override
    protected void intercambiar(int[] datos, int i, int j) {
        movimientos++;
        super.intercambiar(datos, i, j);
    }

    // ------------------------------------------------------------------ inserción

    public void insercionDirecta(int[] datos){
        int n = datos.length;
        for (int i = 1; i < n; i++){
            int aux = datos[i];
            int j = i-1;
            while (j >= 0 && menor(aux, datos[j])){
                datos[j+1] = datos[j];
                movimientos++;
                j = j-1;
            }
            if (j + 1 != i) {
                datos[j+1] = aux;
                movimientos++;
            }
        }
    }

    /**
     * Ordena el vector utilizando shellsort con un vector de incrementos datos.
     *
     * Como en las diapositivas, Inc[1..T] va de menor a mayor (1, 4, 13, ...) y se recorre
     * de T hasta 1, o sea, aplicando primero el incremento más grande. TipoClasificador pasa
     * la secuencia al revés (n/2, n/4, ..., 1), así que se detecta el orden del vector y
     * siempre se aplica de mayor a menor, terminando con el incremento 1.
     * Ver {@link SecuenciasIncrementos} para las secuencias de Shell, Hibbard, Knuth y Ciura.
     */
    public void shell(int[] datos, Integer[] incrementos) {
        int n = datos.length;
        int t = incrementos.length;
        boolean ascendente = t < 2 || incrementos[0] <= incrementos[t - 1];
        for (int paso = 0; paso < t; paso++) {
            int h = ascendente ? incrementos[t - 1 - paso] : incrementos[paso];
            for (int i = h; i < n; i++) {
                int aux = datos[i];
                int j = i - h;
                while (j >= 0 && menor(aux, datos[j])) {
                    datos[j + h] = datos[j];
                    movimientos++;
                    j = j - h;
                }
                if (j + h != i) {
                    datos[j + h] = aux;
                    movimientos++;
                }
            }
        }
    }

    // ------------------------------------------------------------------ intercambio

    /**
     * Ordena el vector utilizando la técnica de Burbuja
     */
    public void burbuja(int[] datos) {
        int n = datos.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = n - 1; j > i; j--) {
                if (menor(datos[j], datos[j - 1])) {
                    intercambiar(datos, j, j - 1);
                }
            }
        }
    }

    // quicksort(int[]) y su versión recursiva vienen de TClasificador y no se modifican
    // (contrato del pivote del README). Acá solo se implementan obtenerPivote y particion.

    /**
     * método auxiliar y utilizado en quicksort
     * devuelve un valor del vector ubicado entre i <= x < j
     * Estrategia: el mayor de los dos primeros elementos distintos del rango
     * (-1 si todos los elementos del rango son iguales).
     */
    protected int obtenerPivote(int[] datos, int i, int j) {
        for (int k = i + 1; k <= j; k++) {
            if (menor(datos[i], datos[k])) {
                return k;
            }
            if (menor(datos[k], datos[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * método auxiliar y utilizado en quicksort
     * deja a la izquierda los menores que el pivote y a la derecha los mayores o iguales;
     * devuelve el comienzo de la parte derecha
     */
    protected int particion(int[] datos, int i, int j, int pivote) {
        int izq = i;
        int der = j;
        while (izq <= der) {
            while (menor(datos[izq], pivote)) {
                izq++;
            }
            while (!menor(datos[der], pivote)) {
                der--;
            }
            if (izq < der) {
                intercambiar(datos, izq, der);
            }
        }
        return izq;
    }

    // ------------------------------------------------------------------ selección

    /**
     * Ordena el vector utilizando clasificación directa (selección directa).
     * Pseudocódigo de clase: en cada pasada busca el menor de V[i..n] y lo intercambia con V[i].
     * Solo intercambia si el menor no está ya en su lugar.
     */
    public void clasificacionDirecta(int[] datos){
        int n = datos.length;
        for (int i = 0; i < n - 1; i++) {
            int indiceDelMenor = i;
            int menorValor = datos[i];
            for (int j = i + 1; j < n; j++) {
                if (menor(datos[j], menorValor)) {
                    indiceDelMenor = j;
                    menorValor = datos[j];
                }
            }
            if (indiceDelMenor != i) {
                intercambiar(datos, i, indiceDelMenor);
            }
        }
    }

    /**
     * Ordena el vector utilizando heapsort (iterativo, sin memoria adicional).
     *
     * Sigue el algoritmo de clase con posiciones 1..N:
     * <pre>
     * Desde i = N div 2 hasta 1:  DesplazaElemento(i, N)        // arma el montículo
     * Desde i = N hasta 2:        Intercambia(V[1], V[i])       // saca la raíz al final
     *                             DesplazaElemento(1, i - 1)
     * </pre>
     * Diferencia con las diapositivas: allí el montículo es de mínimo (la raíz es el menor) y al
     * mandar la raíz al final el vector queda en orden DEScendente. Para obtener orden ascendente
     * se usa un montículo de máximo: en DesplazaElemento se elige el MAYOR hijo.
     */
    public void heapsort(int[] datos) {
        int n = datos.length;
        for (int i = n / 2; i >= 1; i--) {
            desplazaElemento(datos, i, n);
        }
        for (int i = n; i >= 2; i--) {
            intercambiar(datos, 0, i - 1);      // Intercambia(V[1], V[i])
            desplazaElemento(datos, 1, i - 1);
        }
    }

    /**
     * método auxiliar y utilizado en heapsort (iterativo, como en la diapositiva).
     * primero y ultimo son posiciones 1..N: la posición p es datos[p - 1].
     * Hunde el elemento de la posición primero hasta que sea mayor o igual que sus hijos,
     * considerando solo las posiciones primero..ultimo.
     */
    protected void desplazaElemento(int[] datos, int primero, int ultimo) {
        int actual = primero;
        while (actual <= ultimo / 2) {
            int hijo = 2 * actual;
            if (ultimo == hijo) { // actual tiene un solo hijo
                if (menor(datos[actual - 1], datos[hijo - 1])) {
                    intercambiar(datos, actual - 1, hijo - 1);
                }
                actual = ultimo;
            } else {
                // índice del hijo con mayor valor
                int mayor = menor(datos[hijo - 1], datos[hijo]) ? hijo + 1 : hijo;
                if (menor(datos[actual - 1], datos[mayor - 1])) {
                    intercambiar(datos, actual - 1, mayor - 1);
                    actual = mayor;
                } else {
                    actual = ultimo;
                }
            }
        }
    }

    // ------------------------------------------------------------------ enumeración y distribución

    /**
     * Ordena el vector utilizando cuenta por distribución (algoritmo de clase):
     * cuenta las ocurrencias de cada clave entre u = mínimo y v = máximo, acumula, y recorre
     * R de N a 1 ubicando cada elemento en el área de salida S (estable).
     * Usa memoria O(v - u) para Cuenta y O(n) para S.
     */
    public void cuentaPorDistribucion(int[] datos) {
        int n = datos.length;
        if (n < 2) {
            return;
        }
        int[] rango = obtenerRango(datos);
        int u = rango[0];
        int[] cuenta = new int[rango[1] - u + 1];
        for (int clave : datos) {
            cuenta[clave - u]++;
        }
        for (int i = 1; i < cuenta.length; i++) {
            cuenta[i] = cuenta[i] + cuenta[i - 1];
        }
        int[] salida = new int[n];
        for (int j = n - 1; j >= 0; j--) {
            int i = --cuenta[datos[j] - u];
            salida[i] = datos[j];
            movimientos++;
        }
        System.arraycopy(salida, 0, datos, 0, n);
    }

    /**
     * Ordena el vector utilizando binsort (clasificación por urnas), con una urna por cada valor
     * de clave entre el mínimo y el máximo. Admite claves repetidas: cada urna es una lista
     * (enlazada mediante índices: cabeza, cola y siguiente) y se insertan al final, por lo que
     * es estable. Después se concatenan las urnas en orden.
     */
    public void binsort(int[] datos) {
        int n = datos.length;
        if (n < 2) {
            return;
        }
        int[] rango = obtenerRango(datos);
        int u = rango[0];
        int cantidadUrnas = rango[1] - u + 1;
        int[] cabeza = new int[cantidadUrnas];
        int[] cola = new int[cantidadUrnas];
        int[] siguiente = new int[n];
        Arrays.fill(cabeza, -1);
        for (int j = 0; j < n; j++) {
            int urna = datos[j] - u;
            siguiente[j] = -1;
            if (cabeza[urna] == -1) {
                cabeza[urna] = j;
            } else {
                siguiente[cola[urna]] = j;
            }
            cola[urna] = j;
        }
        int[] original = datos.clone();
        int pos = 0;
        for (int urna = 0; urna < cantidadUrnas; urna++) {
            for (int j = cabeza[urna]; j != -1; j = siguiente[j]) {
                datos[pos++] = original[j];
                movimientos++;
            }
        }
    }

    /**
     * Ordena el vector utilizando bucketsort: distribuye los elementos en cantidadUrnas urnas
     * por intervalos de valores (de igual amplitud entre el mínimo y el máximo), ordena cada
     * urna por inserción directa y las concatena.
     */
    public void bucketsort(int[] datos, int cantidadUrnas) {
        int n = datos.length;
        if (n < 2) {
            return;
        }
        if (cantidadUrnas < 1) {
            throw new IllegalArgumentException("Se necesita al menos una urna");
        }
        int[] rango = obtenerRango(datos);
        int minimo = rango[0];
        long amplitud = (long) rango[1] - minimo + 1;
        // inicio[b] .. inicio[b+1]-1 son las posiciones de la urna b en el vector de salida
        int[] inicio = new int[cantidadUrnas + 1];
        for (int valor : datos) {
            inicio[urna(valor, minimo, amplitud, cantidadUrnas) + 1]++;
        }
        for (int b = 1; b <= cantidadUrnas; b++) {
            inicio[b] += inicio[b - 1];
        }
        int[] siguienteLibre = Arrays.copyOf(inicio, cantidadUrnas);
        int[] salida = new int[n];
        for (int valor : datos) {
            salida[siguienteLibre[urna(valor, minimo, amplitud, cantidadUrnas)]++] = valor;
            movimientos++;
        }
        System.arraycopy(salida, 0, datos, 0, n);
        for (int b = 0; b < cantidadUrnas; b++) {
            insercionEnRango(datos, inicio[b], inicio[b + 1] - 1);
        }
    }

    private static int urna(int valor, int minimo, long amplitud, int cantidadUrnas) {
        return (int) ((valor - (long) minimo) * cantidadUrnas / amplitud);
    }

    /** inserción directa sobre datos[desde..hasta] (usada por bucketsort para cada urna) */
    private void insercionEnRango(int[] datos, int desde, int hasta) {
        for (int i = desde + 1; i <= hasta; i++) {
            int aux = datos[i];
            int j = i - 1;
            while (j >= desde && menor(aux, datos[j])) {
                datos[j + 1] = datos[j];
                movimientos++;
                j--;
            }
            if (j + 1 != i) {
                datos[j + 1] = aux;
                movimientos++;
            }
        }
    }

    /**
     * Ordena el vector utilizando radix sort (clasificación por residuos) en la base dada:
     * una pasada estable por cada dígito, del menos significativo al más significativo.
     * Precondición: todos los valores del vector son mayores o iguales a 0, y base >= 2
     */
    public void radixsort(int[] datos, int base) {
        if (base < 2) {
            throw new IllegalArgumentException("La base debe ser >= 2");
        }
        if (datos.length < 2) {
            return;
        }
        int[] rango = obtenerRango(datos);
        if (rango[0] < 0) {
            throw new IllegalArgumentException("radixsort requiere valores >= 0");
        }
        int maximo = rango[1];
        // exponente es long para que exponente * base no desborde; mientras se usa, es <= maximo
        for (long exponente = 1; maximo / exponente > 0; exponente *= base) {
            distribuirPorDigito(datos, (int) exponente, base);
        }
    }

    /**
     * método auxiliar y utilizado en radixsort
     * realiza una pasada ESTABLE de distribución según el dígito (valor / exponente) % base
     * (cuenta por distribución sobre el dígito)
     */
    protected void distribuirPorDigito(int[] datos, int exponente, int base) {
        int n = datos.length;
        int[] cuenta = new int[base];
        for (int valor : datos) {
            cuenta[(valor / exponente) % base]++;
        }
        for (int d = 1; d < base; d++) {
            cuenta[d] += cuenta[d - 1];
        }
        int[] salida = new int[n];
        for (int j = n - 1; j >= 0; j--) {
            int digito = (datos[j] / exponente) % base;
            salida[--cuenta[digito]] = datos[j];
            movimientos++;
        }
        System.arraycopy(salida, 0, datos, 0, n);
    }
}
