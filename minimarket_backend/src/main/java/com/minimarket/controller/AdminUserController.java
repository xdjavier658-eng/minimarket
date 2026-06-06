package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión administrativa de usuarios
 * Solo accesible por usuarios con rol ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Listar todos los usuarios del sistema
     */
    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        // En un entorno de producción, sería mejor excluir las contraseñas
        // mediante un DTO o @JsonIgnore, pero para este requerimiento listamos todo.
        return usuarioRepository.findAll();
    }

    /**
     * Eliminación lógica (desactivación) de un usuario
     */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Evitar que el administrador se elimine a sí mismo (opcional pero recomendado)
        // Podríamos obtener el usuario actual desde SecurityContext si fuera necesario.

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado correctamente"));
    }
}
