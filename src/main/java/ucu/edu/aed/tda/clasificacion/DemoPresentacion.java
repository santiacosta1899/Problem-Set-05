package ucu.edu.aed.tda.clasificacion;

import ucu.edu.aed.tda.clasificacion.datos.GeneradorLecturas;
import ucu.edu.aed.tda.clasificacion.datos.Medidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Demo para la defensa (diapositiva DEMO). Tarda unos segundos y tiene tres pasos:
 * 1) el contrato del pivote con [1, 1, 1, 2];
 * 2) shellsort Ciura contra heapsort (y quicksort de referencia) con las 2.000.000 de lecturas
 *    de un día, semilla 2026: las comparaciones y movimientos son los mismos de las diapositivas;
 * 3) los percentiles del día, ordenando y por conteo.
 *
 * Entre paso y paso espera Enter, así se puede explicar con calma. Si no hay teclado
 * (por ejemplo, corriendo sin consola) sigue de largo.
 */
public class DemoPresentacion {

    private static final Locale ES = Locale.forLanguageTag("es-UY");
    private static final BufferedReader TECLADO = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        titulo("DEMO · Consigna 5 · La estación meteorológica");

        pasoPivote();
        esperarEnter();

        int[] lecturas = GeneradorLecturas.generarLecturas(2_000_000, 2026);
        pasoMedicion(lecturas);
        esperarEnter();

        pasoPercentiles(lecturas);
        System.out.println();
        System.out.println("Fin de la demo.");
    }

    // ------------------------------------------------------------------ paso 1

    private static void pasoPivote() {
        titulo("Paso 1 · Contrato del pivote con [1, 1, 1, 2]");
        System.out.println("La partición deja a la izquierda los MENORES que el pivote: el pivote tiene que");
        System.out.println("ser mayor que el mínimo del rango, si no la parte izquierda queda vacía.");
        System.out.println();
        probarPivote("Mayor de los dos primeros distintos", new Clasificador());
        probarPivote("Mediana de tres (sin corregir)", new ClasificadorMedianaDeTres(false));
        probarPivote("Mediana de tres (corregida)", new ClasificadorMedianaDeTres(true));
    }

    private static void probarPivote(String estrategia, Clasificador c) {
        int[] v = {1, 1, 1, 2};
        String resultado;
        try {
            c.quicksort(v);
            resultado = "ordena bien -> " + Arrays.toString(v);
        } catch (ArrayIndexOutOfBoundsException e) {
            resultado = "FALLA: elige el 1 (el mínimo) y el índice der se sale del vector";
        } catch (StackOverflowError e) {
            resultado = "FALLA: la recursión recibe siempre el mismo rango (StackOverflowError)";
        }
        System.out.printf(ES, "  %-38s %s%n", estrategia, resultado);
    }

    // ------------------------------------------------------------------ paso 2

    private static void pasoMedicion(int[] lecturas) {
        titulo(String.format(ES, "Paso 2 · Ordenar las %,d lecturas de un día", lecturas.length));
        System.out.println("Cada algoritmo ordena una copia de los mismos datos y el Medidor de la cátedra");
        System.out.println("verifica que quede ordenado. Tiempo: mediana de 3 corridas.");
        System.out.println();
        System.out.printf(ES, "  %-24s %10s %18s %18s%n", "algoritmo", "tiempo", "comparaciones", "movimientos");

        Clasificador c = new Clasificador();
        Integer[] ciura = SecuenciasIncrementos.CIURA.generar(lecturas.length);
        long[] shell = medir("Shellsort Ciura", d -> c.shell(d, ciura), c, lecturas);
        long[] heap = medir("Heapsort", c::heapsort, c, lecturas);
        medir("Quicksort (referencia)", c::quicksort, c, lecturas);

        System.out.println();
        System.out.printf(ES, "  Heapsort hace %.1f veces más comparaciones y %.1f veces más movimientos que Ciura.%n",
                (double) heap[0] / shell[0], (double) heap[1] / shell[1]);
    }

    /** imprime una fila y devuelve {comparaciones, movimientos} */
    private static long[] medir(String nombre, Consumer<int[]> algoritmo, Clasificador c, int[] datos) {
        int[] copia = datos.clone();
        c.reiniciarContadores();
        algoritmo.accept(copia);
        if (!Medidor.mismoContenidoOrdenado(datos, copia)) {
            throw new IllegalStateException(nombre + " no ordenó bien");
        }
        long comparaciones = c.getComparaciones();
        long movimientos = c.getMovimientos();
        double ms = Medidor.mediana(Medidor.medirNanos(algoritmo, datos, 3, 1)) / 1e6;
        System.out.printf(ES, "  %-24s %7.1f ms %18s %18s%n", nombre, ms,
                String.format(ES, "%,d", comparaciones), String.format(ES, "%,d", movimientos));
        return new long[]{comparaciones, movimientos};
    }

    // ------------------------------------------------------------------ paso 3

    private static void pasoPercentiles(int[] lecturas) {
        titulo("Paso 3 · Percentiles del día");
        int[] ordenado = lecturas.clone();
        new Clasificador().shell(ordenado, SecuenciasIncrementos.CIURA.generar(ordenado.length));
        int[] pOrdenado = Percentiles.desdeOrdenado(ordenado);

        // se repite 5 veces y se toma la más rápida: la primera incluye la compilación de la JVM
        int[] pConteo = null;
        double msConteo = Double.MAX_VALUE;
        for (int r = 0; r < 5; r++) {
            long inicio = System.nanoTime();
            pConteo = Percentiles.porConteo(lecturas);
            msConteo = Math.min(msConteo, (System.nanoTime() - inicio) / 1e6);
        }

        boolean coinciden = Arrays.equals(pOrdenado, pConteo);
        System.out.printf(ES, "  Ordenando con Ciura:  p1 = %s   p50 = %s   p99 = %s%n",
                Percentiles.grados(pOrdenado[1]), Percentiles.grados(pOrdenado[50]), Percentiles.grados(pOrdenado[99]));
        System.out.printf(ES, "  Por conteo:           p1 = %s   p50 = %s   p99 = %s%n",
                Percentiles.grados(pConteo[1]), Percentiles.grados(pConteo[50]), Percentiles.grados(pConteo[99]));
        System.out.println();
        System.out.printf(ES, "  El conteo usa 1.101 contadores (4,4 KB), no compara lecturas y tardó %.1f ms.%n", msConteo);
        System.out.println("  ¿Coinciden los 99 percentiles? " + (coinciden ? "sí" : "NO"));
    }

    // ------------------------------------------------------------------ salida

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(86));
        System.out.println("  " + texto);
        System.out.println("=".repeat(86));
    }

    private static void esperarEnter() {
        System.out.println();
        System.out.print("  [Enter para seguir] ");
        System.out.flush();
        try {
            if (TECLADO.readLine() == null) {
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println();
        }
    }
}
