package com.minimarket.controller;

import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.dto.venta.FinalizarVentaResponse;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:8080")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/finalizar")
    public ResponseEntity<FinalizarVentaResponse> finalizarVenta(
            @Valid @RequestBody FinalizarVentaRequest request) {
        
        // Obtener el usuario autenticado desde el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Usuario vendedor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado: " + username));
        
        FinalizarVentaResponse response = ventaService.finalizarVenta(request, vendedor);
        
        return ResponseEntity.ok(response);
    }
}
