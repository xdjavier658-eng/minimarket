package com.minimarket.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para la gestión de archivos multimedia (imágenes de productos)
 */
@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    // Ruta donde se guardarán las imágenes físicamente
    // Usar un directorio absoluto para consistencia
    private final Path root = Paths.get(System.getProperty("user.dir"), "uploads", "img");

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                logger.info("✓ Directorio de imágenes creado: " + root.toString());
            }
        } catch (IOException e) {
            logger.error("✗ Error al crear directorio de imágenes: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validar tamaño del archivo (máximo 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "El archivo es demasiado grande (máximo 5MB)");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Solo se permiten archivos de imagen");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // El directorio ya está creado en @PostConstruct

            // Generar un nombre único para evitar duplicados
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                // Validar extensiones permitidas
                if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Tipo de archivo no permitido. Solo JPG, PNG, GIF, WebP");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            String filename = UUID.randomUUID().toString() + extension;

            // Guardar el archivo
            Path targetFile = root.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Imagen subida exitosamente: " + filename);

            // Retornar solo el nombre del archivo para guardarlo en la base de datos
            Map<String, String> response = new HashMap<>();
            response.put("filename", filename);
            response.put("url", "/img/" + filename);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error al subir archivo", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al subir archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path file = root.resolve(filename).normalize();

            // Verificar que el archivo esté dentro del directorio root (seguridad)
            if (!file.startsWith(root)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determinar el tipo de contenido basado en la extensión
                String contentType = "application/octet-stream"; // default
                String lowerFilename = filename.toLowerCase();
                if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerFilename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowerFilename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowerFilename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("Error al procesar URL del archivo: " + filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
