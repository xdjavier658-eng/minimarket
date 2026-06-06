package com.minimarket.config;

import com.minimarket.entity.ERole;
import com.minimarket.entity.Role;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RoleRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            logger.info("=== INICIANDO CARGA DE DATOS ===");

            // 🛠️ PARCHE DE BASE DE DATOS: Asegurar que la columna 'activo' exista en 'productos'
            try {
                jdbcTemplate.execute("ALTER TABLE productos ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE");
                logger.info("✓ Base de datos verificada: Columna 'activo' en productos");
            } catch (Exception e) {
                logger.warn("No se pudo verificar la columna 'activo' (puede que ya exista o la tabla no esté creada): {}", e.getMessage());
            }
            
            // Crear roles si no existen
            try {
                createRoleIfNotExists(roleRepository, ERole.ADMIN);
                createRoleIfNotExists(roleRepository, ERole.VENDEDOR);
                createRoleIfNotExists(roleRepository, ERole.ALMACENERO);
                logger.info("✓ Roles verificados/creados: ADMIN, VENDEDOR, ALMACENERO");
            } catch (Exception e) {
                logger.error("Error al crear roles: {}", e.getMessage());
            }

            // Crear usuario admin si no existe
            try {
                Optional<Usuario> existingAdmin = usuarioRepository.findByUsername("admin");
                
                if (existingAdmin.isEmpty()) {
                    logger.info("Creando usuario administrador...");

                    String rawPassword = "admin123";
                    String encodedPassword = passwordEncoder.encode(rawPassword);

                    Usuario admin = new Usuario();
                    admin.setUsername("admin");
                    admin.setPassword(encodedPassword);
                    admin.setActivo(true);

                    Role adminRole = roleRepository.findByNombre(ERole.ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Rol ADMIN no encontrado"));

                    admin.getRoles().add(adminRole);

                    usuarioRepository.save(admin);
                    
                    logger.info("✓ Usuario admin creado exitosamente");
                    logger.info("╔════════════════════════════════════════╗");
                    logger.info("║  CREDENCIALES DE ACCESO               ║");
                    logger.info("║  Username: admin                      ║");
                    logger.info("║  Password: admin123                   ║");
                    logger.info("╚════════════════════════════════════════╝");
                } else {
                    logger.info("✓ Usuario admin ya existe");
                    logger.info("  Username: admin");
                    logger.info("  Password: admin123");
                }
            } catch (Exception e) {
                logger.error("Error al crear usuario admin: {}", e.getMessage(), e);
            }

            logger.info("=== CARGA DE DATOS COMPLETADA ===\n");
        };
    }
    
    private void createRoleIfNotExists(RoleRepository roleRepository, ERole roleEnum) {
        Optional<Role> existingRole = roleRepository.findByNombre(roleEnum);
        if (existingRole.isEmpty()) {
            Role role = new Role(roleEnum);
            roleRepository.save(role);
            logger.debug("Rol creado: {}", roleEnum);
        } else {
            logger.debug("Rol ya existe: {}", roleEnum);
        }
    }
}