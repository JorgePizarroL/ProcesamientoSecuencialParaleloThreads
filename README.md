# Procesamiento secuencial y paralelo de textos con Java Threads

Esta practica evalúa el impacto de la aceleración (*speedup*) y el rendimiento computacional al aplicar programación multihilo en Java frente a un enfoque secuencial tradicional. La aplicación procesa el texto completo de *Don Quijote de la Mancha* para extraer métricas estadísticas en tiempo real.

---

## ¿Qué hace el programa?
Lee el texto completo de la obra y calcula tres cosas principales:

1. El total de palabras en todo el archivo.
2. Cuántas veces aparece exactamente la palabra **"Quijote"**.
3. Cuántas palabras contienen o terminan con la secuencia **"ción"**.

Además, mide el tiempo exacto que tarda cada método

---

## Estructura del Proyecto

```text
.
├── QuijoteProcessor.java   # Módulo principal (Lógica secuencial + paralela con hilos)
├── quijote.txt             # Dataset de texto original
├── informe.html            # Reporte técnico interactivo de resultados
└── README.md               # Documentación del proyecto
```

## Requisitos

1. Java Development Kit (JDK): Versión 25 o superior (desarrollado y probado sobre JDK 25).


## ¿Cómo ejecutarlo?

1. **Clonar este repositorio:**

   git clone https://github.com/JorgePizarroL/ProcesamientoSecuencialParaleloThreads.git

2. Descargar el libro Don Quijote de la Mancha en formato TXT desde Project Gutenberg renombrar quijote.txt colocarlo en la raiz del proyecto.

3. Compilar el código: javac QuijoteProcessor.java

4. Correr el programa: java QuijoteProcessor

## Resultados y Reporte

Al ejecutarlo verás en la consola los tiempos en milisegundos para 1, 2 y 4 hilos. 