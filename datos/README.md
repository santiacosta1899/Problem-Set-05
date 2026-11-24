# Datos de la consigna 5

Archivos generados con la semilla 2026. Son exactamente los mismos datos que produce el generador.

| Archivo | Contenido |
|---|---|
| `lecturas-1-dia.txt` | 2.000.000 de lecturas de un día, en décimas de grado, una por línea y en el orden en que se tomaron. |
| `lecturas-5-dias.txt` | 10.000.000 de lecturas de cinco días seguidos, con el mismo formato. |

## Cómo leerlos

Con la clase `ArchivoDatos` del proyecto (paquete `ucu.edu.aed.tda.clasificacion.datos`):

```java
ArchivoDatos.leerEnteros(Path.of("datos/lecturas-1-dia.txt"))
```

La ruta es relativa a la carpeta desde donde ejecutan el programa: ajústenla si hace falta. Con estos volúmenes, ejecuten con `-Xmx4g`.

## Cómo generar otras variantes

El generador está en `proyecto/src/main/java/ucu/edu/aed/tda/clasificacion/datos/GeneradorLecturas.java`. Estos archivos se obtuvieron con:

```java
GeneradorLecturas.generarLecturas(2_000_000, 2026) y GeneradorLecturas.generarLecturas(10_000_000, 5, 2026)
```

Los tamaños menores para extrapolar se generan cambiando el primer parámetro.
