package ucu.edu.aed.tda.clasificacion;
import ucu.edu.aed.tda.clasificacion.TClasificador;

public class Clasificador extends TClasificador {

    public void insercionDirecta(int[] datos){
        int n = datos.length;
        for (int i = 1; i < n; i++){
            int aux = datos[i];
            int j = i-1;
            while (j >= 0 && aux < datos[j]){
                datos[j+1] = datos[j];
                j = j-1;
            }
            datos[j+1] = aux;
        }
    }

    /**
     * Ordena el vector utilizando shellsort con un vector de incrementos datos
     */
    public void shell(int[] datos, Integer[] incrementos) {
    int n = datos.length;
    int t = incrementos.length;
    for (int k = t - 1; k >= 0; k--) {
        int h = incrementos[k];
        for (int i = h; i < n; i++) {
            int aux = datos[i];
            int j = i - h;
            while (j >= 0 && aux < datos[j]) {
                datos[j + h] = datos[j];
                j = j - h;
            }
            datos[j + h] = aux;
        }
    }
    }

    /**
     * Ordena el vector utilizando la técnica de Burbuja
     */
    public void burbuja(int[] datos) {
    int n = datos.length;
    for (int i = 0; i < n - 1; i++) {
        for (int j = n - 1; j > i; j--) {
            if (datos[j] < datos[j - 1]) {
                intercambiar(datos, j, j - 1);
            }
        }
    }
    }


    public void quicksort(int[] datos) {
        quicksort(datos, 0, datos.length - 1);
    }

    /**
     * método recursivo de quicksort
     */
    private void quicksort(int[] datos, int i, int j) {
        if (i <= j) {
            int indicePivote = obtenerPivote(datos, i, j);
            // debe ser un rango válido
            if (indicePivote >= i && indicePivote < j) {
                int pivote = datos[indicePivote];
                int k = particion(datos, i, j, pivote);
                quicksort(datos, i, k - 1);
                quicksort(datos, k, j);
            }
        }
    }

    /**
     * método auxiliar y utilizado en quicksort
     * devuelve un valor del vector ubicado entre i <= x < j
     */
    protected int obtenerPivote(int[] datos, int i, int j) {
        for (int k = i + 1; k <= j; k++) {
            if (datos[k] > datos[i]) {
                return k;
            }
            if (datos[k] < datos[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * método auxiliar y utilizado en quicksort
     */
    protected int particion(int[] datos, int i, int j, int pivote) {
        int izq = i;
        int der = j;
        while (izq <= der) {
            while (datos[izq] < pivote) {
                izq++;
            }
            while (datos[der] >= pivote) {
                der--;
            }
            if (izq < der) {
                intercambiar(datos, izq, der);
            }
        }
        return izq;
    }

    /**
     * Ordena el vector utilizando clasificación directa
     */
    public void clasificacionDirecta(int[] datos){
        // TODO
        throw new UnsupportedOperationException("clasificacionDirecta: sin implementar");
    }

    /**
     * Ordena el vector utilizando heapsort
     */
    public void heapsort(int[] datos) {
        // TODO
        throw new UnsupportedOperationException("heapsort: sin implementar");
    }

    /**
     * método auxiliar y utilizado en heapsort
     */
    protected void desplazaElemento(int[] datos, int primero, int ultimo) {
        // TODO
        throw new UnsupportedOperationException("desplazaElemento: sin implementar");
    }

    /**
     * Ordena el vector utilizando cuenta por distribución (clasificación por enumeración o cuenta).
     * Admite claves repetidas; el rango de claves va del mínimo al máximo del vector
     */
    public void cuentaPorDistribucion(int[] datos) {
        // TODO
        throw new UnsupportedOperationException("cuentaPorDistribucion: sin implementar");
    }

    /**
     * Ordena el vector utilizando binsort (clasificación por urnas), con una urna por cada
     * valor de clave entre el mínimo y el máximo del vector. Debe admitir claves repetidas
     */
    public void binsort(int[] datos) {
        // TODO
        throw new UnsupportedOperationException("binsort: sin implementar");
    }

    /**
     * Ordena el vector utilizando bucketsort: distribuye los elementos en cantidadUrnas urnas
     * por intervalos de valores, ordena cada urna (por ejemplo, por inserción) y las concatena
     */
    public void bucketsort(int[] datos, int cantidadUrnas) {
        // TODO
        throw new UnsupportedOperationException("bucketsort: sin implementar");
    }

    /**
     * Ordena el vector utilizando radix sort (clasificación por residuos) en la base dada.
     * Precondición: todos los valores del vector son mayores o iguales a 0, y base >= 2
     */
    public void radixsort(int[] datos, int base) {
        // TODO
        throw new UnsupportedOperationException("radixsort: sin implementar");
    }

    /**
     * método auxiliar y utilizado en radixsort
     * realiza una pasada ESTABLE de distribución según el dígito (valor / exponente) % base
     */
    protected void distribuirPorDigito(int[] datos, int exponente, int base) {
        // TODO
        throw new UnsupportedOperationException("distribuirPorDigito: sin implementar");
    }

    /**
     * método de ayuda para intercambiar valores en un vector dados sus índices
     */
    protected void intercambiar(int[] datos, int i, int j) {
        int aux = datos[i];
        datos[i] = datos[j];
        datos[j] = aux;
    }

    /**
     * calcula el tiempo de ejecución de una tarea en milisegundos
     */
    public long medirTiempoDeEjecución(Runnable tarea) {
        long start = System.currentTimeMillis();

        tarea.run();

        return (System.currentTimeMillis() - start);
    }

   
    
    
}
