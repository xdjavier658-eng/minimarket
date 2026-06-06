import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TreePrinter {

    // Carpetas a ignorar
    private static final Set<String> IGNORED_DIRS = new HashSet<>(Arrays.asList(
            ".git",
            ".vscode",
            ".idea",
            "target",
            "node_modules",
            "dist",
            "build"
    ));

    // Extensiones de archivos a ignorar
    private static final Set<String> IGNORED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".class",
            ".log",
            ".iml",
            ".lock",
            ".tmp"
    ));

    // Archivos específicos a ignorar
    private static final Set<String> IGNORED_FILES = new HashSet<>(Arrays.asList(
            ".DS_Store"
    ));

    public static void main(String[] args) {
        File root = args.length > 0 ? new File(args[0]) : new File(".");

        if (!root.exists()) {
            System.err.println("La ruta no existe");
            return;
        }

        File output = new File("arbol.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            writer.println(root.getCanonicalFile().getName());
            printTree(root, "", writer);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        System.out.println("Archivo arbol.txt creado en: " + output.getAbsolutePath());
    }

    private static void printTree(File dir, String prefix, PrintWriter writer) {
        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            boolean isLast = (i == files.length - 1);

            if (shouldIgnore(file)) continue;

            writer.println(prefix +
                    (isLast ? "\\-- " : "|-- ") +
                    file.getName());

            if (file.isDirectory()) {
                printTree(
                        file,
                        prefix + (isLast ? "    " : "|   "),
                        writer
                );
            }
        }
    }

    private static boolean shouldIgnore(File file) {
        String name = file.getName();

        if (file.isDirectory()) {
            return IGNORED_DIRS.contains(name);
        }

        if (IGNORED_FILES.contains(name)) {
            return true;
        }

        for (String ext : IGNORED_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
