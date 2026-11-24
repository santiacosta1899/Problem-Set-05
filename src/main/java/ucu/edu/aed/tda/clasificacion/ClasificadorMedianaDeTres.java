package ucu.edu.aed.tda.clasificacion;

/**
 * Contrato del pivote (README, preguntas 3 y 4): quicksort con la estrategia
 * «la mediana entre el primero, el del medio y el último».
 *
 * Con la particion de Clasificador (izquierda: menores que el pivote; derecha: mayores o iguales),
 * el quicksort solo avanza si el pivote es MAYOR que el mínimo del rango. La mediana de tres no lo
 * garantiza: en [1, 1, 1, 2] la mediana de (1, 1, 2) es 1, que es el mínimo; la parte izquierda
 * queda vacía, quicksort(k, j) recibe el mismo rango y la recursión no termina (StackOverflowError).
 *
 * Con corregido = true: si la mediana es igual al menor de los tres, se usa el mayor de los tres;
 * como el menor de los tres está en el rango, el pivote queda estrictamente mayor que el mínimo
 * del rango. Si los tres son iguales se recurre a «el mayor de los dos primeros distintos»
 * (que devuelve -1 si todo el rango es igual).
 */
public class ClasificadorMedianaDeTres extends Clasificador {

    private final boolean corregido;

    public ClasificadorMedianaDeTres(boolean corregido) {
        this.corregido = corregido;
    }

    @Override
    protected int obtenerPivote(int[] datos, int i, int j) {
        int m = (i + j) / 2;
        int a = datos[i];
        int b = datos[m];
        int c = datos[j];
        int minimo = Math.min(a, Math.min(b, c));
        int maximo = Math.max(a, Math.max(b, c));
        if (minimo == maximo) {
            // los tres son iguales: hay que mirar el resto del rango
            return super.obtenerPivote(datos, i, j);
        }
        int mediana = a + b + c - minimo - maximo;
        int valor = mediana;
        if (corregido && mediana == minimo) {
            valor = maximo;
        }
        return indiceMenorQueJ(datos, i, m, j, valor);
    }

    /**
     * Devuelve un índice x con i <= x < j que contenga valor (el contrato pide x < j).
     * Si el valor solo está en j, lo intercambia con el del medio.
     */
    private int indiceMenorQueJ(int[] datos, int i, int m, int j, int valor) {
        if (datos[i] == valor) {
            return i;
        }
        if (datos[m] == valor && m < j) {
            return m;
        }
        intercambiar(datos, m, j);
        return m;
    }
}
