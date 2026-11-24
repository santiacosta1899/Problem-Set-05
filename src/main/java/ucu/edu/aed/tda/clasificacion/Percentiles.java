package ucu.edu.aed.tda.clasificacion;

/**
 * Cálculo de percentiles de las lecturas (en décimas de grado).
 *
 * Definición usada (rango más cercano): el percentil p de n valores ordenados es el que está
 * en la posición ceil(p * n / 100), contando desde 1; en Java, el índice ceil(p * n / 100) - 1.
 * Ejemplo con n = 2.000.000: p50 está en la posición 1.000.000 (índice 999.999).
 */
public final class Percentiles {

    /** Rango físico de la estación: -50,0 a 60,0 °C, en décimas de grado. */
    public static final int MINIMO_DECIMAS = -500;
    public static final int MAXIMO_DECIMAS = 600;

    private Percentiles() {
    }

    /** Índice (0..n-1) del percentil p en un vector ordenado de largo n. */
    public static int indice(int p, int n) {
        if (p < 1 || p > 99 || n < 1) {
            throw new IllegalArgumentException("p debe estar entre 1 y 99 y n >= 1");
        }
        long posicion = ((long) p * n + 99) / 100; // ceil(p * n / 100), posición 1..n
        return (int) Math.max(posicion, 1) - 1;
    }

    /** Percentiles 1..99 tomados de un vector YA ORDENADO. resultado[p] es el percentil p. */
    public static int[] desdeOrdenado(int[] ordenado) {
        int[] resultado = new int[100];
        for (int p = 1; p <= 99; p++) {
            resultado[p] = ordenado[indice(p, ordenado.length)];
        }
        return resultado;
    }

    /**
     * Pregunta 15: percentiles 1..99 SIN ordenar ni comparar elementos entre sí.
     * Como las claves están en un rango conocido y chico (-500..600 décimas), se cuenta cuántas
     * veces aparece cada valor (1.101 contadores, unos 4,4 KB) y se recorren las cuentas
     * acumuladas hasta llegar a la posición de cada percentil. Tiempo O(n + rango), memoria
     * O(rango) (independiente de n), sin recursión, y no modifica el vector de lecturas.
     * resultado[p] es el percentil p.
     */
    public static int[] porConteo(int[] lecturas) {
        int n = lecturas.length;
        int[] cuenta = new int[MAXIMO_DECIMAS - MINIMO_DECIMAS + 1];
        for (int valor : lecturas) {
            if (valor < MINIMO_DECIMAS || valor > MAXIMO_DECIMAS) {
                throw new IllegalArgumentException("Lectura fuera del rango del sensor: " + valor);
            }
            cuenta[valor - MINIMO_DECIMAS]++;
        }
        int[] resultado = new int[100];
        int p = 1;
        long acumulado = 0;
        for (int k = 0; k < cuenta.length && p <= 99; k++) {
            acumulado += cuenta[k];
            // el valor k cubre las posiciones (acumulado - cuenta[k], acumulado]
            while (p <= 99 && indice(p, n) < acumulado) {
                resultado[p] = k + MINIMO_DECIMAS;
                p++;
            }
        }
        return resultado;
    }

    /** décimas de grado a texto en °C, ej. 183 -> "18,3 °C" */
    public static String grados(int decimas) {
        String signo = decimas < 0 ? "-" : "";
        int abs = Math.abs(decimas);
        return signo + (abs / 10) + "," + (abs % 10) + " °C";
    }
}
