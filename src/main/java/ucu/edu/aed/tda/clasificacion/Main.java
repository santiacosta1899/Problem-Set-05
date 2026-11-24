package ucu.edu.aed.tda.clasificacion;

import ucu.edu.aed.tda.clasificacion.datos.ArchivoDatos;
import ucu.edu.aed.tda.clasificacion.datos.GeneradorLecturas;
import ucu.edu.aed.tda.clasificacion.datos.Medidor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Consigna 5 · La estación meteorológica.
 *
 * Uso (desde la carpeta del proyecto, para que encuentre datos/ y escriba en resultados/):
 * <pre>
 *   java -Xmx4g -Xss512m ... ucu.edu.aed.tda.clasificacion.Main [parte] [rapido]
 * </pre>
 * parte: pivote | exploracion | candidatos | permutacion | cuadraticos | percentiles | todo
 * (por defecto: todo).
 * rapido: tamaños 10 veces más chicos y límite de 5 s, para probar o para la corrida en vivo.
 *
 * Mide con Medidor: 1 corrida contada (comparaciones y movimientos, y verificación del orden),
 * después 1 de calentamiento y la mediana de 3. Si la corrida contada pasa el límite de 60 s
 * se informa ese único tiempo y no se agranda más ese algoritmo (regla de corte del README).
 * Los resultados se imprimen y se guardan en resultados/*.csv para armar tablas y gráficos.
 */
public class Main {

    private static final long SEMILLA = 2026;
    private static final int REPETICIONES = 3;
    private static final int CALENTAMIENTOS = 1;

    /** Secuencia usada para calcular los percentiles. Cámbienla si sus mediciones indican otra. */
    private static final SecuenciasIncrementos SECUENCIA_ELEGIDA = SecuenciasIncrementos.CIURA;

    private static int nDia = 2_000_000;
    private static int nCincoDias = 10_000_000;
    private static int nInicialCuadraticos = 10_000;
    private static long limiteMs = 60_000;
    private static boolean rapido = false;

    private static final Path CARPETA_DATOS = Path.of("datos");
    private static final Path CARPETA_RESULTADOS = Path.of("resultados");

    public static void main(String[] args) throws InterruptedException {
        // Se corre en un hilo con pila de 512 MB (lo mismo que -Xss512m), así el quicksort
        // recursivo tiene la pila que pide el README aunque no se configure la VM.
        Thread hilo = new Thread(null, () -> {
            try {
                correr(args);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        }, "consigna-5", 512L * 1024 * 1024);
        hilo.start();
        hilo.join();
    }

    private static void correr(String[] args) {
        String parte = "todo";
        for (String a : args) {
            if (a.equalsIgnoreCase("rapido")) {
                rapido = true;
            } else {
                parte = a.toLowerCase(Locale.ROOT);
            }
        }
        if (rapido) {
            nDia = 200_000;
            nCincoDias = 1_000_000;
            nInicialCuadraticos = 2_000;
            limiteMs = 5_000;
        }
        crearCarpeta(CARPETA_RESULTADOS);
        System.out.printf("Consigna 5 · estación meteorológica | parte=%s | semilla=%d | n día=%,d | n 5 días=%,d%s%n",
                parte, SEMILLA, nDia, nCincoDias, rapido ? " | MODO RÁPIDO" : "");
        System.out.printf("Memoria máxima de la VM: %,d MB%n%n", Runtime.getRuntime().maxMemory() / (1024 * 1024));

        boolean todo = parte.equals("todo");
        if (todo || parte.equals("pivote")) {
            contratoDelPivote();
        }
        if (todo || parte.equals("exploracion")) {
            parteA_exploracion();
        }
        if (todo || parte.equals("candidatos")) {
            parteC_candidatos();
        }
        if (todo || parte.equals("permutacion")) {
            permutacionAlAzar();
        }
        if (todo || parte.equals("cuadraticos")) {
            parteC_cuadraticos();
        }
        if (todo || parte.equals("percentiles")) {
            parteD_E_percentiles();
        }
        if (!todo && !List.of("pivote", "exploracion", "candidatos", "permutacion", "cuadraticos", "percentiles")
                .contains(parte)) {
            System.out.println("Parte desconocida: " + parte + ". Opciones: pivote | exploracion | candidatos"
                    + " | permutacion | cuadraticos | percentiles | todo [rapido]");
        }
    }

    // =================================================================== contrato del pivote

    /** Preguntas del README sobre el pivote: prueba las estrategias con [1, 1, 1, 2] y con las lecturas. */
    private static void contratoDelPivote() {
        titulo("Contrato del pivote · [1, 1, 1, 2] y lecturas de un día");
        Map<String, Clasificador> estrategias = new LinkedHashMap<>();
        estrategias.put("Mayor de los dos primeros distintos", new Clasificador());
        estrategias.put("Mediana de tres (sin corregir)", new ClasificadorMedianaDeTres(false));
        estrategias.put("Mediana de tres (corregida)", new ClasificadorMedianaDeTres(true));
        for (Map.Entry<String, Clasificador> e : estrategias.entrySet()) {
            int[] v = {1, 1, 1, 2};
            String resultado;
            try {
                e.getValue().quicksort(v);
                resultado = java.util.Arrays.toString(v) + (Medidor.estaOrdenado(v) ? " ordenado" : " NO ordenado");
            } catch (StackOverflowError err) {
                resultado = "StackOverflowError: la recursión no termina";
            } catch (ArrayIndexOutOfBoundsException err) {
                resultado = "ArrayIndexOutOfBoundsException: ningún elemento es menor que el pivote, "
                        + "der se sale del vector (" + err.getMessage() + ")";
            }
            System.out.printf("  %-38s [1, 1, 1, 2] -> %s%n", e.getKey(), resultado);
        }
        System.out.println();
        int[] dia = lecturasDia(nDia);
        encabezadoTabla();
        String[] etiquetas = {"QS 2 primeros distintos", "QS mediana 3 sin corr.", "QS mediana 3 corregida"};
        int indice = 0;
        for (Clasificador c : estrategias.values()) {
            String etiqueta = etiquetas[indice++];
            try {
                imprimir(medir(etiqueta, c::quicksort, c, "1 día", dia));
            } catch (ArrayIndexOutOfBoundsException err) {
                System.out.printf("%-24s %,12d  ArrayIndexOutOfBoundsException (%s)%n",
                        etiqueta, dia.length, err.getMessage());
            }
        }
        System.out.println();
    }

    // =================================================================== permutación al azar

    /**
     * Pregunta obligatoria: «si los datos fueran una permutación al azar del mismo tamaño, ¿cambiaría
     * su elección?». Se miden dos casos:
     * 1) las mismas lecturas de un día mezcladas al azar (mismos valores y repetidos, sin orden temporal);
     * 2) una permutación al azar de 0..n-1 (sin orden temporal y sin repetidos).
     * Comparando con las lecturas originales se separa el efecto del orden del de los repetidos.
     */
    private static void permutacionAlAzar() {
        titulo("Permutación al azar: lecturas mezcladas y permutación de 0..n-1");
        int[] mezclado = lecturasDia(nDia).clone();
        java.util.Random rnd = new java.util.Random(SEMILLA);
        for (int i = mezclado.length - 1; i > 0; i--) { // Fisher-Yates
            int k = rnd.nextInt(i + 1);
            int aux = mezclado[i];
            mezclado[i] = mezclado[k];
            mezclado[k] = aux;
        }
        List<Resultado> resultados = new ArrayList<>();
        medirCandidatos(new Clasificador(), "1 día mezclado", mezclado, new HashMap<>(), resultados);

        // permutación al azar de 0..n-1: mismo tamaño pero sin orden temporal NI valores repetidos
        int[] permutacion = new int[nDia];
        for (int i = 0; i < nDia; i++) {
            permutacion[i] = i;
        }
        for (int i = permutacion.length - 1; i > 0; i--) {
            int k = rnd.nextInt(i + 1);
            int aux = permutacion[i];
            permutacion[i] = permutacion[k];
            permutacion[k] = aux;
        }
        medirCandidatos(new Clasificador(), "permutación 0..n-1", permutacion, new HashMap<>(), resultados);
        guardarCsv("permutacion.csv", resultados);
    }

    // =================================================================== datos

    /** lecturas de un día con n lecturas */
    private static int[] lecturasDia(int n) {
        return cargar("lecturas-1-dia.txt", n, 1);
    }

    /** lecturas de cinco días con n lecturas en total */
    private static int[] lecturasCincoDias(int n) {
        return cargar("lecturas-5-dias.txt", n, 5);
    }

    /**
     * Si existe el archivo de datos/ con exactamente n lecturas lo lee con ArchivoDatos;
     * si no, las genera con GeneradorLecturas (son los mismos datos, misma semilla).
     */
    private static int[] cargar(String archivo, int n, int dias) {
        Path ruta = CARPETA_DATOS.resolve(archivo);
        boolean esElArchivo = (dias == 1 && n == 2_000_000) || (dias == 5 && n == 10_000_000);
        if (esElArchivo && Files.exists(ruta)) {
            int[] datos = ArchivoDatos.leerEnteros(ruta);
            if (datos.length == n) {
                return datos;
            }
        }
        return GeneradorLecturas.generarLecturas(n, dias, SEMILLA);
    }

    // =================================================================== Parte A

    private static void parteA_exploracion() {
        titulo("Parte A · Exploración de los datos");
        explorar("1 día", lecturasDia(nDia), 1);
        explorar("5 días", lecturasCincoDias(nCincoDias), 5);
    }

    private static void explorar(String nombre, int[] d, int dias) {
        int n = d.length;
        int minimo = d[0];
        int maximo = d[0];
        for (int x : d) {
            minimo = Math.min(minimo, x);
            maximo = Math.max(maximo, x);
        }
        boolean[] aparece = new boolean[maximo - minimo + 1];
        int distintos = 0;
        long suben = 0;
        long bajan = 0;
        long iguales = 0;
        for (int i = 0; i < n; i++) {
            if (!aparece[d[i] - minimo]) {
                aparece[d[i] - minimo] = true;
                distintos++;
            }
            if (i > 0) {
                if (d[i] > d[i - 1]) {
                    suben++;
                } else if (d[i] < d[i - 1]) {
                    bajan++;
                } else {
                    iguales++;
                }
            }
        }
        long pares = Math.max(1, n - 1);
        System.out.printf("[%s] n=%,d  mínimo=%s  máximo=%s  valores distintos=%d (rango de %d décimas)%n",
                nombre, n, Percentiles.grados(minimo), Percentiles.grados(maximo), distintos, maximo - minimo + 1);
        System.out.printf("  pares consecutivos: suben %.1f%%  bajan %.1f%%  iguales %.1f%%%n",
                100.0 * suben / pares, 100.0 * bajan / pares, 100.0 * iguales / pares);

        // forma del día: promedio por hora (en el caso de 5 días, bloques de 6 h)
        int bloquesPorDia = dias == 1 ? 24 : 4;
        int bloques = bloquesPorDia * dias;
        System.out.printf("  promedio por bloque de %d h (°C):%n   ", 24 / bloquesPorDia);
        for (int b = 0; b < bloques; b++) {
            int desde = (int) ((long) n * b / bloques);
            int hasta = (int) ((long) n * (b + 1) / bloques);
            long suma = 0;
            for (int i = desde; i < hasta; i++) {
                suma += d[i];
            }
            int promedio = (int) Math.round((double) suma / Math.max(1, hasta - desde));
            System.out.printf(" %s", Percentiles.grados(promedio).replace(" °C", ""));
            if ((b + 1) % (dias == 1 ? 12 : bloquesPorDia) == 0 && b + 1 < bloques) {
                System.out.print("\n   ");
            }
        }
        System.out.println();

        // muestra para graficar las lecturas en el orden en que se tomaron
        String archivo = "lecturas-muestra-" + (dias == 1 ? "1-dia" : "5-dias") + ".csv";
        int paso = Math.max(1, n / 5_000);
        try (PrintWriter out = abrirCsv(archivo)) {
            out.println("indice,decimas,grados");
            for (int i = 0; i < n; i += paso) {
                out.printf(Locale.ROOT, "%d,%d,%.1f%n", i, d[i], d[i] / 10.0);
            }
        }
        System.out.printf("  muestra para graficar (1 de cada %,d lecturas): resultados/%s%n%n", paso, nombreCsv(archivo));
    }

    // =================================================================== Parte C · candidatos

    private static void parteC_candidatos() {
        titulo("Parte C · Candidatos (shellsort x 4 secuencias, heapsort) y quicksort de referencia");
        Clasificador c = new Clasificador();
        Map<String, String> cortados = new HashMap<>();
        List<Resultado> resultados = new ArrayList<>();

        // serie que se duplica con datos de un día, hasta el tamaño de la consigna
        List<Integer> tamanios = new ArrayList<>();
        for (int n = nDia / 32; n < nDia; n *= 2) {
            tamanios.add(n);
        }
        tamanios.add(nDia);

        for (int n : tamanios) {
            medirCandidatos(c, "1 día", lecturasDia(n), cortados, resultados);
        }
        medirCandidatos(c, "5 días", lecturasCincoDias(nCincoDias), cortados, resultados);
        guardarCsv("candidatos.csv", resultados);
    }

    private static void medirCandidatos(Clasificador c, String conjunto, int[] datos,
                                        Map<String, String> cortados, List<Resultado> resultados) {
        System.out.printf("%n-- %s, n = %,d --%n", conjunto, datos.length);
        encabezadoTabla();
        for (SecuenciasIncrementos s : SecuenciasIncrementos.values()) {
            Integer[] inc = s.generar(datos.length);
            medirConCorte("Shellsort " + s.name(), d -> c.shell(d, inc), c, conjunto, datos, cortados, resultados);
        }
        medirConCorte("Heapsort", c::heapsort, c, conjunto, datos, cortados, resultados);
        medirConCorte("Quicksort (referencia)", c::quicksort, c, conjunto, datos, cortados, resultados);
    }

    // =================================================================== Parte C · O(n^2)

    private static void parteC_cuadraticos() {
        titulo("Parte C · Inserción directa, burbuja y selección: serie de tamaños y extrapolación");
        System.out.printf("Regla de corte: si una corrida supera %d s no se sigue agrandando ese algoritmo.%n",
                limiteMs / 1000);
        Clasificador c = new Clasificador();
        List<Resultado> resultados = new ArrayList<>();
        Map<String, Consumer<int[]>> algoritmos = new LinkedHashMap<>();
        algoritmos.put("Inserción directa", c::insercionDirecta);
        algoritmos.put("Burbuja", c::burbuja);
        algoritmos.put("Selección directa", c::clasificacionDirecta);

        for (Map.Entry<String, Consumer<int[]>> alg : algoritmos.entrySet()) {
            System.out.printf("%n-- %s --%n", alg.getKey());
            encabezadoTabla();
            List<Resultado> serie = new ArrayList<>();
            for (int n = nInicialCuadraticos; n <= nDia; n *= 2) {
                Resultado r = medir(alg.getKey(), alg.getValue(), c, "1 día", lecturasDia(n));
                imprimir(r);
                serie.add(r);
                resultados.add(r);
                if (r.ms() > limiteMs) {
                    System.out.printf("  corte: superó %d s con n = %,d%n", limiteMs / 1000, n);
                    break;
                }
            }
            extrapolar(alg.getKey(), serie, nDia);
        }
        guardarCsv("cuadraticos.csv", resultados);
    }

    /**
     * Extrapola al tamaño objetivo desde la última medición: con el modelo teórico t = a·n²
     * y con el exponente medido entre las dos últimas mediciones (t2/t1 = (n2/n1)^b).
     */
    private static void extrapolar(String nombre, List<Resultado> serie, int objetivo) {
        Resultado ultimo = serie.get(serie.size() - 1);
        if (ultimo.n() >= objetivo) {
            System.out.printf("  %s llegó a medirse con n = %,d: no hace falta extrapolar%n", nombre, objetivo);
            return;
        }
        double factor = (double) objetivo / ultimo.n();
        double teorico = ultimo.ms() * factor * factor;
        double comps = ultimo.comparaciones() * factor * factor;
        double movs = ultimo.movimientos() * factor * factor;
        System.out.printf("  Extrapolación a n = %,d desde n = %,d con t ∝ n²: %s  (comparaciones ≈ %.3e, movimientos ≈ %.3e)%n",
                objetivo, ultimo.n(), duracion(teorico), comps, movs);
        if (serie.size() >= 2) {
            Resultado anterior = serie.get(serie.size() - 2);
            double b = Math.log(ultimo.ms() / anterior.ms()) / Math.log((double) ultimo.n() / anterior.n());
            double empirico = ultimo.ms() * Math.pow(factor, b);
            System.out.printf("  Exponente medido entre n = %,d y n = %,d: b = %.2f  ->  t ≈ %s%n",
                    anterior.n(), ultimo.n(), b, duracion(empirico));
        }
    }

    // =================================================================== Partes D y E · percentiles

    private static void parteD_E_percentiles() {
        titulo("Partes D y E · Percentiles");
        Clasificador c = new Clasificador();
        percentiles(c, "1 día", lecturasDia(nDia));
        percentiles(c, "5 días", lecturasCincoDias(nCincoDias));
    }

    private static void percentiles(Clasificador c, String conjunto, int[] lecturas) {
        int n = lecturas.length;
        System.out.printf("%n-- %s, n = %,d --%n", conjunto, n);

        // pregunta 13: ordenar con el algoritmo elegido y tomar las posiciones
        int[] ordenado = lecturas.clone();
        Integer[] inc = SECUENCIA_ELEGIDA.generar(n);
        c.reiniciarContadores();
        long t0 = System.nanoTime();
        c.shell(ordenado, inc);
        double msOrdenar = (System.nanoTime() - t0) / 1e6;
        if (!Medidor.mismoContenidoOrdenado(lecturas, ordenado)) {
            throw new IllegalStateException("El shellsort elegido no ordenó bien");
        }
        int[] pOrdenado = Percentiles.desdeOrdenado(ordenado);
        System.out.printf("Ordenando con shellsort %s (%s; %,d comparaciones, %,d movimientos):%n",
                SECUENCIA_ELEGIDA.name(), duracion(msOrdenar), c.getComparaciones(), c.getMovimientos());
        for (int p : new int[]{1, 50, 99}) {
            System.out.printf("  p%-2d (posición %,d) = %s%n", p, Percentiles.indice(p, n) + 1,
                    Percentiles.grados(pOrdenado[p]));
        }
        System.out.print("  Resumen por radio, p1..p99 (décimas):");
        for (int p = 1; p <= 99; p++) {
            System.out.print((p - 1) % 20 == 0 ? "\n   " : " ");
            System.out.print(pOrdenado[p]);
        }
        System.out.println();

        // pregunta 15: por conteo, sin comparar elementos entre sí
        int[] pConteo = Percentiles.porConteo(lecturas);
        boolean coinciden = true;
        for (int p = 1; p <= 99; p++) {
            coinciden &= pConteo[p] == pOrdenado[p];
        }
        double msConteo = Medidor.mediana(Medidor.medirNanos(Percentiles::porConteo, lecturas,
                REPETICIONES, CALENTAMIENTOS, false)) / 1e6;
        int contadores = Percentiles.MAXIMO_DECIMAS - Percentiles.MINIMO_DECIMAS + 1;
        System.out.printf("Por conteo (pregunta 15): %s, %d contadores (%,d bytes), no modifica el vector, "
                        + "coincide con el ordenado: %s%n",
                duracion(msConteo), contadores, contadores * Integer.BYTES, coinciden ? "sí" : "NO");
    }

    // =================================================================== medición

    record Resultado(String conjunto, String algoritmo, int n, double ms,
                     long comparaciones, long movimientos, String nota) {
    }

    /** mide respetando la regla de corte: si el algoritmo ya se cortó en un tamaño menor, no se mide */
    private static void medirConCorte(String nombre, Consumer<int[]> algoritmo, Clasificador c, String conjunto,
                                      int[] datos, Map<String, String> cortados, List<Resultado> resultados) {
        if (cortados.containsKey(nombre)) {
            Resultado r = new Resultado(conjunto, nombre, datos.length, Double.NaN, -1, -1,
                    "no se midió: " + cortados.get(nombre));
            imprimir(r);
            resultados.add(r);
            return;
        }
        Resultado r = medir(nombre, algoritmo, c, conjunto, datos);
        imprimir(r);
        resultados.add(r);
        if (Double.isNaN(r.ms())) {
            cortados.put(nombre, r.nota() + " con n = " + datos.length);
        } else if (r.ms() > limiteMs) {
            cortados.put(nombre, "superó " + limiteMs / 1000 + " s con n = " + datos.length);
        }
    }

    /**
     * Una corrida contada (comparaciones, movimientos y verificación) y, si no pasó el límite,
     * la mediana de REPETICIONES corridas con Medidor (con CALENTAMIENTOS previos).
     */
    private static Resultado medir(String nombre, Consumer<int[]> algoritmo, Clasificador c,
                                   String conjunto, int[] datos) {
        int[] copia = datos.clone();
        c.reiniciarContadores();
        long inicio = System.nanoTime();
        try {
            algoritmo.accept(copia);
        } catch (StackOverflowError e) {
            return new Resultado(conjunto, nombre, datos.length, Double.NaN,
                    c.getComparaciones(), c.getMovimientos(), "StackOverflowError");
        }
        double msContado = (System.nanoTime() - inicio) / 1e6;
        if (!Medidor.mismoContenidoOrdenado(datos, copia)) {
            throw new IllegalStateException(nombre + " no ordenó correctamente con n = " + datos.length);
        }
        long comparaciones = c.getComparaciones();
        long movimientos = c.getMovimientos();
        if (msContado > limiteMs) {
            return new Resultado(conjunto, nombre, datos.length, msContado, comparaciones, movimientos,
                    "1 sola corrida (superó el límite)");
        }
        double ms = Medidor.mediana(Medidor.medirNanos(algoritmo, datos, REPETICIONES, CALENTAMIENTOS)) / 1e6;
        return new Resultado(conjunto, nombre, datos.length, ms, comparaciones, movimientos, "");
    }

    // =================================================================== salida

    private static void titulo(String texto) {
        System.out.println("=".repeat(100));
        System.out.println(texto);
        System.out.println("=".repeat(100));
    }

    private static void encabezadoTabla() {
        System.out.printf("%-24s %12s %14s %20s %20s  %s%n",
                "algoritmo", "n", "mediana", "comparaciones", "movimientos", "nota");
    }

    private static void imprimir(Resultado r) {
        String tiempo = Double.isNaN(r.ms()) ? "-" : duracion(r.ms());
        String comps = r.comparaciones() < 0 ? "-" : String.format("%,d", r.comparaciones());
        String movs = r.movimientos() < 0 ? "-" : String.format("%,d", r.movimientos());
        System.out.printf("%-24s %,12d %14s %20s %20s  %s%n", r.algoritmo(), r.n(), tiempo, comps, movs, r.nota());
    }

    private static String duracion(double ms) {
        if (ms < 1_000) {
            return String.format("%.1f ms", ms);
        }
        if (ms < 120_000) {
            return String.format("%.2f s", ms / 1_000);
        }
        if (ms < 2 * 3_600_000) {
            return String.format("%.1f min", ms / 60_000);
        }
        if (ms < 2 * 86_400_000.0) {
            return String.format("%.1f h", ms / 3_600_000);
        }
        return String.format("%.1f días", ms / 86_400_000);
    }

    private static void guardarCsv(String archivo, List<Resultado> resultados) {
        try (PrintWriter out = abrirCsv(archivo)) {
            out.println("conjunto,algoritmo,n,mediana_ms,comparaciones,movimientos,nota");
            for (Resultado r : resultados) {
                out.printf(Locale.ROOT, "%s,%s,%d,%s,%s,%s,%s%n", r.conjunto(), r.algoritmo(), r.n(),
                        Double.isNaN(r.ms()) ? "" : String.format(Locale.ROOT, "%.3f", r.ms()),
                        r.comparaciones() < 0 ? "" : r.comparaciones(),
                        r.movimientos() < 0 ? "" : r.movimientos(),
                        r.nota().replace(',', ';'));
            }
        }
        System.out.printf("%nResultados guardados en resultados/%s%n%n", nombreCsv(archivo));
    }

    /** en modo rápido los CSV llevan el sufijo -rapido para no pisar los resultados reales */
    private static String nombreCsv(String archivo) {
        return rapido ? archivo.replace(".csv", "-rapido.csv") : archivo;
    }

    private static PrintWriter abrirCsv(String archivo) {
        try {
            return new PrintWriter(Files.newBufferedWriter(CARPETA_RESULTADOS.resolve(nombreCsv(archivo)),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void crearCarpeta(Path carpeta) {
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
