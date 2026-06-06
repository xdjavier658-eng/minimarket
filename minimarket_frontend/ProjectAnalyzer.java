import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ProjectAnalyzer {
    
    // Extensiones de archivos a incluir
    private static final Set<String> INCLUDED_EXTENSIONS = Set.of(
        ".java", ".yml", ".yaml", ".properties", ".xml", 
        ".sql", ".txt", ".md", ".json", ".html", ".css", ".js"
    );
    
    // Carpetas y archivos a ignorar
    private static final Set<String> IGNORED_DIRS = Set.of(
        ".git", ".vscode", ".idea", "node_modules", "target",
        "build", "dist", "out", "__pycache__", ".metadata"
    );
    
    private static final Set<String> IGNORED_FILES = Set.of(
        ".gitignore", ".env", ".env.local", ".env.production",
        ".classpath", ".project", ".settings", "Thumbs.db",
        "Desktop.ini", ".DS_Store", "compilado.txt"
    );
    
    // Archivos específicos a incluir
    private static final Set<String> SPECIFIC_FILES = Set.of(
        "pom.xml", "build.gradle", "package.json", "README.md",
        "Dockerfile", "docker-compose.yml", "Makefile", "package-lock.json "
    );
    
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        System.out.println("ANALIZANDO DIRECTORIO: " + currentDir);
        
        try {
            String output = analyzeProject(Paths.get(currentDir));
            
            // Guardar en archivo
            Path outputFile = Paths.get(currentDir, "compilado.txt");
            Files.writeString(outputFile, output, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("ARCHIVO CREADO: " + outputFile.toAbsolutePath());
            System.out.println("TOTAL LINEAS: " + output.lines().count());
            
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String analyzeProject(Path rootDir) throws IOException {
        StringBuilder result = new StringBuilder();
        
        // Cabecera
        result.append("=".repeat(80)).append("\n");
        result.append("PROYECTO: ").append(rootDir.getFileName()).append("\n");
        result.append("RUTA: ").append(rootDir.toAbsolutePath()).append("\n");
        result.append("FECHA: ").append(new Date()).append("\n");
        result.append("=".repeat(80)).append("\n\n");
        
        // Recorrer el árbol de directorios
        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
            private int fileCount = 0;
            private int dirCount = 0;
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String dirName = dir.getFileName().toString();
                
                // Ignorar carpetas no deseadas
                if (IGNORED_DIRS.contains(dirName) || dirName.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                // No incluir la carpeta raíz en el conteo
                if (!dir.equals(rootDir)) {
                    dirCount++;
                    result.append("\n[DIR] ").append(getRelativePath(rootDir, dir)).append("/\n");
                }
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                
                // Ignorar archivos no deseados
                if (IGNORED_FILES.contains(fileName) || fileName.startsWith(".")) {
                    return FileVisitResult.CONTINUE;
                }
                
                // Verificar si el archivo debe ser incluido
                if (shouldIncludeFile(file)) {
                    fileCount++;
                    result.append("\n[FILE] ").append(getRelativePath(rootDir, file)).append("\n");
                    result.append("-".repeat(60)).append("\n");
                    
                    try {
                        String content = Files.readString(file);
                        result.append(content).append("\n");
                    } catch (IOException e) {
                        result.append("[ERROR: No se pudo leer el archivo - ").append(e.getMessage()).append("]\n");
                    }
                    
                    result.append("-".repeat(60)).append("\n");
                }
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Estadísticas al final
                if (dir.equals(rootDir)) {
                    result.append("\n").append("=".repeat(80)).append("\n");
                    result.append("ESTADISTICAS:\n");
                    result.append("  Carpetas analizadas: ").append(dirCount).append("\n");
                    result.append("  Archivos incluidos: ").append(fileCount).append("\n");
                    result.append("  Tamanio total: ").append(result.length()).append(" caracteres\n");
                    result.append("=".repeat(80)).append("\n");
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        return result.toString();
    }
    
    private static boolean shouldIncludeFile(Path file) {
        String fileName = file.getFileName().toString();
        
        // Incluir archivos específicos
        if (SPECIFIC_FILES.contains(fileName)) {
            return true;
        }
        
        // Verificar extensión
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = fileName.substring(dotIndex).toLowerCase();
            return INCLUDED_EXTENSIONS.contains(extension);
        }
        
        return false;
    }
    
    private static String getRelativePath(Path root, Path target) {
        return root.relativize(target).toString().replace("\\", "/");
    }
}