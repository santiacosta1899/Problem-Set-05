package ucu.edu.aed.tda.clasificacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Secuencias de incrementos para shellsort (Parte B, pregunta 5 de la consigna 5).
 *
 * Todas se devuelven como en las diapositivas: Inc[1..T] de menor a mayor (1, ..., mayor),
 * con incrementos menores que n. Clasificador.shell las recorre de mayor a menor.
 */
public enum SecuenciasIncrementos {

    /** Shell (1959): n/2, n/4, ..., 1. Peor caso O(n^2) (los incrementos se pueden ir dividiendo de a pares). */
    SHELL("Shell (n/2, n/4, ..., 1)", "O(n^2) en el peor caso"),

    /** Hibbard (1963): 2^k - 1 = 1, 3, 7, 15, 31, ... Peor caso O(n^(3/2)). */
    HIBBARD("Hibbard (1, 3, 7, 15, ..., 2^k - 1)", "O(n^(3/2)) en el peor caso"),

    /** Knuth (1973): (3^k - 1) / 2 = 1, 4, 13, 40, 121, ... Peor caso O(n^(3/2)). */
    KNUTH("Knuth (1, 4, 13, 40, ..., (3^k - 1)/2)", "O(n^(3/2)) en el peor caso"),

    /**
     * Ciura (2001): 1, 4, 10, 23, 57, 132, 301, 701, 1750, obtenidos experimentalmente.
     * Para n más grandes se extiende con h(k+1) = floor(2,25 * h(k)), la extensión habitual.
     * No tiene un análisis teórico conocido; es la mejor secuencia empírica conocida.
     */
    CIURA("Ciura (1, 4, 10, 23, 57, 132, 301, 701, 1750, x2,25)", "sin cota teórica conocida (empírica)");

    private static final int[] CIURA_BASE = {1, 4, 10, 23, 57, 132, 301, 701, 1750};

    private final String nombre;
    private final String complejidad;

    SecuenciasIncrementos(String nombre, String complejidad) {
        this.nombre = nombre;
        this.complejidad = complejidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getComplejidad() {
        return complejidad;
    }

    /** Incrementos para un vector de largo n, de menor a mayor (siempre empieza en 1). */
    public Integer[] generar(int n) {
        List<Integer> inc = new ArrayList<>();
        switch (this) {
            case SHELL -> {
                for (int h = n / 2; h >= 1; h /= 2) {
                    inc.add(0, h);
                }
            }
            case HIBBARD -> {
                for (long h = 1; h < n; h = 2 * h + 1) {
                    inc.add((int) h);
                }
            }
            case KNUTH -> {
                for (long h = 1; h < n; h = 3 * h + 1) {
                    inc.add((int) h);
                }
            }
            case CIURA -> {
                long h = 1;
                for (int base : CIURA_BASE) {
                    if (base >= n) {
                        break;
                    }
                    inc.add(base);
                    h = base;
                }
                if (h == CIURA_BASE[CIURA_BASE.length - 1]) {
                    for (h = (long) (h * 2.25); h < n; h = (long) (h * 2.25)) {
                        inc.add((int) h);
                    }
                }
            }
        }
        if (inc.isEmpty()) {
            inc.add(1);
        }
        return inc.toArray(new Integer[0]);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
