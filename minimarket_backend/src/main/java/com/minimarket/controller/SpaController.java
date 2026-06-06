package com.minimarket.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador SPA (Single Page Application)
 *
 * Redirige rutas del frontend al index.html de React para que React Router
 * maneje la navegación en el cliente.
 *
 * Lógica de exclusión:
 *  - /api/**           → manejado por los @RestControllers del backend
 *  - /img/**           → imágenes de productos (servidas por ResourceHandler)
 *  - /assets/**        → archivos estáticos del build de Vite
 *  - cualquier ruta con extensión (.js, .css, etc.) → servida como recurso estático
 */
@Controller
public class SpaController {

    /**
     * Captura todas las rutas que NO empiecen con /api ni /img
     * y NO tengan extensión de archivo.
     * Estas son rutas de React Router que necesitan index.html.
     */
    @RequestMapping({
        "/admin/**",
        "/catalogo/**",
        "/productos/**",
        "/pago-exitoso/**",
        "/pago-fallido/**",
        "/pago-pendiente/**",
        "/ticket/**"
    })
    public String forward(HttpServletRequest request) {
        return "forward:/index.html";
    }
}
