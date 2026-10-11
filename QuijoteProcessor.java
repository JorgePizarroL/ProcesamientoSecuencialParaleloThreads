import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QuijoteProcessor {

    public static class Result {
        long wordCount = 0;
        long targetWordCount = 0;
        long substringCount = 0;
    }

    public static void main(String[] args) {
        String filePath = "quijote.txt";
        
        try {
            // Cargar el archivo en memoria antes de iniciar mediciones
            String content = Files.readString(Path.of(filePath));
            content = content.repeat(100);
            
            // Nota: Si luego quieres probar con el documento ampliado (100 veces), 
            // puedes descomentar la siguiente línea:
            // content = content.repeat(100);

            String targetWord = "Quijote";
            String targetSubstring = "ción";
            int runs = 5;

            // --- 1. EJECUCIÓN SECUENCIAL (1 Hilo) ---
            long totalSeqTime = 0;
            Result seqResult = new Result();

            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                seqResult = processSequential(content, targetWord, targetSubstring);
                long end = System.nanoTime();
                totalSeqTime += (end - start);
            }
            double avgSeqTimeMs = (totalSeqTime / (double) runs) / 1_000_000.0;
            
            System.out.println("=== SECUENCIAL (1 Hilo) ===");
            System.out.println("Tiempo promedio (ms): " + avgSeqTimeMs);
            System.out.println("Palabras totales: " + seqResult.wordCount);
            System.out.println("Apariciones de '" + targetWord + "': " + seqResult.targetWordCount);
            System.out.println("Apariciones de subcadena '" + targetSubstring + "': " + seqResult.substringCount);
            System.out.println("----------------------------------------\n");

            // --- 2. EJECUCIÓN PARALELA (2 y 4 Hilos) ---
            int[] threadCounts = {2, 4};
            for (int numThreads : threadCounts) {
                long totalParTime = 0;
                Result parResult = new Result();

                for (int i = 0; i < runs; i++) {
                    long start = System.nanoTime();
                    parResult = processParallel(content, targetWord, targetSubstring, numThreads);
                    long end = System.nanoTime();
                    totalParTime += (end - start);
                }

                double avgParTimeMs = (totalParTime / (double) runs) / 1_000_000.0;
                double speedup = avgSeqTimeMs / avgParTimeMs;

                System.out.println("=== PARALELO (" + numThreads + " Hilos) ===");
                System.out.println("Tiempo promedio (ms): " + avgParTimeMs);
                System.out.println("Aceleración (Speedup S): " + speedup);
                System.out.println("Palabras totales: " + parResult.wordCount);
                System.out.println("----------------------------------------\n");
            }

        } catch (IOException e) {
            System.err.println("No se encontró el archivo: " + e.getMessage());
        }
    }

    // Implementación Secuencial
    public static Result processSequential(String text, String targetWord, String targetSub) {
        Result res = new Result();
        String[] words = text.split("\\s+");
        res.wordCount = words.length;

        Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(targetWord) + "\\b", Pattern.CASE_INSENSITIVE);
        
        for (String w : words) {
            if (wordPattern.matcher(w).find()) {
                res.targetWordCount++;
            }
            if (w.toLowerCase().contains(targetSub.toLowerCase())) {
                res.substringCount++;
            }
        }
        return res;
    }

    // Implementación Paralela (Usando la clase Thread explícitamente)
    public static Result processParallel(String text, String targetWord, String targetSub, int numThreads) {
        int length = text.length();
        int chunkSize = length / numThreads;
        
        List<WorkerThread> workers = new ArrayList<>();
        List<Integer> boundaries = new ArrayList<>();

        boundaries.add(0);
        for (int i = 1; i < numThreads; i++) {
            int pos = i * chunkSize;
            // Evitar cortar palabras a la mitad buscando el siguiente espacio blanco
            while (pos < length && !Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
            boundaries.add(pos);
        }
        boundaries.add(length);

        // Crear y lanzar los hilos
        for (int i = 0; i < numThreads; i++) {
            int startIdx = boundaries.get(i);
            int endIdx = boundaries.get(i + 1);
            String chunk = text.substring(startIdx, endIdx);

            WorkerThread worker = new WorkerThread(chunk, targetWord, targetSub);
            workers.add(worker);
            worker.start(); // Iniciar hilo manualmente
        }

        // Esperar a que todos los hilos terminen con join() y sumar resultados
        Result finalResult = new Result();
        for (WorkerThread worker : workers) {
            try {
                worker.join();
                finalResult.wordCount += worker.getResult().wordCount;
                finalResult.targetWordCount += worker.getResult().targetWordCount;
                finalResult.substringCount += worker.getResult().substringCount;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return finalResult;
    }

    // Clase Hilo independiente
    public static class WorkerThread extends Thread {
        private final String chunk;
        private final String targetWord;
        private final String targetSub;
        private final Result result = new Result();

        public WorkerThread(String chunk, String targetWord, String targetSub) {
            this.chunk = chunk;
            this.targetWord = targetWord;
            this.targetSub = targetSub;
        }

        @Override
        public void run() {
            String[] words = chunk.split("\\s+");
            result.wordCount = words.length;

            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(targetWord) + "\\b", Pattern.CASE_INSENSITIVE);

            for (String w : words) {
                if (wordPattern.matcher(w).find()) {
                    result.targetWordCount++;
                }
                if (w.toLowerCase().contains(targetSub.toLowerCase())) {
                    result.substringCount++;
                }
            }
        }

        public Result getResult() {
            return result;
        }
    }
}
