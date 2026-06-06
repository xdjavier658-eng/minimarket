package com.minimarket.security;

import com.minimarket.security.jwt.AuthEntryPointJwt;
import com.minimarket.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

        @Autowired
        private UserDetailsService userDetailsService;

        @Autowired
        private AuthEntryPointJwt unauthorizedHandler;

        @Autowired
        private JwtAuthFilter jwtAuthFilter;

        @Bean
        public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/static/**",
                                "/img/**",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.png",
                                "/*.svg",
                                "/admin/**",
                                "/productos/**",
                                "/catalogo/**");
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // CSRF off (API REST)
                                .csrf(csrf -> csrf.disable())

                                // Manejo de errores de autenticación
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler))

                                // Stateless
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Autorización
                                .authorizeHttpRequests(auth -> auth
                                                // Permitir redirecciones internas y errores
                                                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD,
                                                                jakarta.servlet.DispatcherType.ERROR)
                                                .permitAll()

                                                // Recursos estáticos estándar y rutas de React
                                                .requestMatchers("/", "/index", "/index.html",
                                                                "/*.js", "/*.css", "/*.ico",
                                                                "/*.png", "/*.svg", "/*.webp",
                                                                "/assets/**", "/static/**", "/img/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // Rutas del frontend (React Router)
                                                .requestMatchers("/admin/**", "/productos/**",
                                                                "/catalogo/**", "/pago-exitoso/**",
                                                                "/pago-fallido/**", "/pago-pendiente/**")
                                                .permitAll()

                                                // Públicos API
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/public/**").permitAll()
                                                .requestMatchers("/api/productos/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers("/api/test/**").permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                .requestMatchers("/api/dashboard/**").hasAuthority("ADMIN")
                                                .requestMatchers("/api/media/**").hasAnyAuthority("ADMIN", "VENDEDOR")

                                                // Protegidos por rol
                                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                                                .requestMatchers("/api/vendedor/**")
                                                .hasAnyAuthority("ADMIN", "VENDEDOR")
                                                .requestMatchers("/api/almacen/**")
                                                .hasAnyAuthority("ADMIN", "ALMACENERO")

                                                // Todo lo demás requiere login
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5500",
                                "http://127.0.0.1:5500",
                                "http://localhost:8080",
                                "http://localhost:3000",
                                "http://localhost:4200",
                                "http://localhost:5173"));

                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }
}
