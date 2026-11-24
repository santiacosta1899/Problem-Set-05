package ucu.edu.aed.tda.clasificacion;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests de los métodos de ordenación de Clasificador: cada uno tiene que dejar el vector
 * igual a lo que deja Arrays.sort, con casos borde, repetidos, negativos, ya ordenados y al revés.
 */
public class ClasificadorTest {

    private Clasificador c;

    @Before
    public void setUp() {
        c = new Clasificador();
    }

    /** Casos borde y vectores al azar (con repetidos y negativos), cada uno también ordenado y al revés. */
    private static List<int[]> entradas() {
        List<int[]> casos = new ArrayList<>();
        casos.add(new int[0]);
        casos.add(new int[]{5});
        casos.add(new int[]{2, 1});
        casos.add(new int[]{1, 2});
        casos.add(new int[]{1, 1, 1, 2});
        casos.add(new int[]{2, 2, 2, 2});
        casos.add(new int[]{2, 5, 3, 0, 2, 3, 0, 3});                                   // ejemplo de clase
        casos.add(new int[]{256, 458, 655, 298, 43, 648, 778, 621, 655, 19, 124, 847}); // vector del PS
        Random rnd = new Random(2026);
        for (int n : new int[]{3, 7, 50, 333, 1000}) {
            for (int rango : new int[]{2, 10, 1000, 1_000_000}) {
                int[] d = new int[n];
                for (int i = 0; i < n; i++) {
                    d[i] = rnd.nextInt(rango) - rango / 3;
                }
                casos.add(d);
                int[] ascendente = d.clone();
                Arrays.sort(ascendente);
                casos.add(ascendente);
                int[] descendente = new int[n];
                for (int i = 0; i < n; i++) {
                    descendente[i] = ascendente[n - 1 - i];
                }
                casos.add(descendente);
            }
        }
        return casos;
    }

    private static void verificaQueOrdena(String nombre, Consumer<int[]> algoritmo) {
        for (int[] entrada : entradas()) {
            int[] esperado = entrada.clone();
            Arrays.sort(esperado);
            int[] obtenido = entrada.clone();
            algoritmo.accept(obtenido);
            String caso = entrada.length <= 12 ? Arrays.toString(entrada) : "vector de " + entrada.length + " elementos";
            assertArrayEquals(nombre + " con " + caso, esperado, obtenido);
        }
    }

    // ------------------------------------------------------------------ los seis algoritmos

    @Test
    public void insercionDirectaOrdena() {
        verificaQueOrdena("inserción directa", c::insercionDirecta);
    }

    @Test
    public void shellOrdenaConCadaSecuencia() {
        for (SecuenciasIncrementos s : SecuenciasIncrementos.values()) {
            verificaQueOrdena("shellsort " + s.name(), d -> c.shell(d, s.generar(d.length)));
        }
    }

    @Test
    public void shellOrdenaConIncrementosDeMayorAMenor() {
        // TipoClasificador pasa los incrementos al revés (n/2, ..., 1): shell tiene que aceptarlos igual
        verificaQueOrdena("shellsort (n/2, ..., 1)", d -> c.shell(d, TipoClasificador.incrementosPorDefecto(d.length)));
    }

    @Test
    public void shellConElEjemploDelPS() {
        int[] v = {256, 458, 655, 298, 43, 648, 778, 621, 655, 19, 124, 847};
        c.shell(v, new Integer[]{1, 3, 5}); // incrementos (5, 3, 1), en el orden de la diapositiva
        assertArrayEquals(new int[]{19, 43, 124, 256, 298, 458, 621, 648, 655, 655, 778, 847}, v);
    }

    @Test
    public void burbujaOrdena() {
        verificaQueOrdena("burbuja", c::burbuja);
    }

    @Test
    public void quicksortOrdena() {
        verificaQueOrdena("quicksort", c::quicksort);
    }

    @Test
    public void seleccionDirectaOrdena() {
        verificaQueOrdena("selección directa", c::clasificacionDirecta);
    }

    @Test
    public void heapsortOrdena() {
        verificaQueOrdena("heapsort", c::heapsort);
    }

    // ------------------------------------------------------------------ métodos de distribución

    @Test
    public void cuentaPorDistribucionOrdena() {
        verificaQueOrdena("cuenta por distribución", c::cuentaPorDistribucion);
    }

    @Test
    public void binsortOrdena() {
        verificaQueOrdena("binsort", c::binsort);
    }

    @Test
    public void bucketsortOrdenaConDistintasCantidadesDeUrnas() {
        for (int urnas : new int[]{1, 3, 10}) {
            verificaQueOrdena("bucketsort " + urnas + " urnas", d -> c.bucketsort(d, urnas));
        }
        verificaQueOrdena("bucketsort n urnas", d -> c.bucketsort(d, Math.max(1, d.length)));
    }

    @Test
    public void radixsortOrdenaEnDistintasBases() {
        // radix exige valores >= 0
        Random rnd = new Random(7);
        for (int base : new int[]{2, 10, 256}) {
            int[] v = new int[500];
            for (int i = 0; i < v.length; i++) {
                v[i] = rnd.nextInt(Integer.MAX_VALUE);
            }
            v[0] = Integer.MAX_VALUE;
            v[1] = 0;
            int[] esperado = v.clone();
            Arrays.sort(esperado);
            c.radixsort(v, base);
            assertArrayEquals("radix base " + base, esperado, v);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void radixsortRechazaNegativos() {
        c.radixsort(new int[]{3, -1, 2});
    }
}
