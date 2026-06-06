package com.minimarket.controller;

import com.minimarket.dto.request.LoginRequest;
import com.minimarket.dto.request.SignupRequest;
import com.minimarket.dto.response.JwtResponse;
import com.minimarket.entity.ERole;
import com.minimarket.entity.Role;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RoleRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.jwt.JwtUtils;
import com.minimarket.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== SIGNIN ====================
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Intento de login para usuario: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("Login exitoso para usuario: {} con roles: {}", userDetails.getUsername(), roles);

            JwtResponse response = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.error("Credenciales inválidas para usuario: {}", loginRequest.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Usuario o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("Error en autenticación: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error en el proceso de autenticación");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== SIGNUP ====================
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Error: El username ya existe"));
        }

        // Crear usuario
        Usuario user = new Usuario(signUpRequest.getUsername(),
                passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // Rol por defecto ALMACENERO
            Role defaultRole = roleRepository.findByNombre(ERole.ALMACENERO)
                    .orElseThrow(() -> new RuntimeException("Error: Rol ALMACENERO no encontrado"));
            roles.add(defaultRole);
        } else {
            strRoles.forEach(roleStr -> {
                switch (roleStr.toUpperCase()) {
                    case "ADMIN":
                        roles.add(roleRepository.findByNombre(ERole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Rol ADMIN no encontrado")));
                        break;
                    case "VENDEDOR":
                        roles.add(roleRepository.findByNombre(ERole.VENDEDOR)
                                .orElseThrow(() -> new RuntimeException("Error: Rol VENDEDOR no encontrado")));
                        break;
                    case "ALMACENERO":
                    default:
                        roles.add(roleRepository.findByNombre(ERole.ALMACENERO)
                                .orElseThrow(() -> new RuntimeException("Error: Rol ALMACENERO no encontrado")));
                }
            });
        }

        user.setRoles(roles);
        usuarioRepository.save(user);

        logger.info("Usuario registrado correctamente: {}", user.getUsername());

        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }

    // ==================== TEST ENDPOINTS ====================
    @GetMapping("/test")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Auth test endpoint works!");
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> response = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            response.put("valid", "true");
            response.put("username", authentication.getName());
            return ResponseEntity.ok(response);
        }

        response.put("valid", "false");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
