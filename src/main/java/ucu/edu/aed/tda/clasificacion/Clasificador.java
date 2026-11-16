package ucu.edu.aed.tda.clasificacion;
import ucu.edu.aed.tda.clasificacion.TClasificador;

public class Clasificador implements TClasificador {

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
    protected abstract int obtenerPivote(int[] datos, int i, int j);

    /**
     * método auxiliar y utilizado en quicksort
     */
    protected abstract int particion(int[] datos, int i, int j, int pivote);

    /**
     * Ordena el vector utilizando clasificación directa
     */
    public abstract void clasificacionDirecta(int[] datos);

    /**
     * Ordena el vector utilizando heapsort
     */
    public abstract void heapsort(int[] datos);

    /**
     * método auxiliar y utilizado en heapsort
     */
    protected abstract void desplazaElemento(int[] datos, int primero, int ultimo);

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
