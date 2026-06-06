package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/productos")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "http://localhost:3000" })
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // GET → listar productos
    @GetMapping
    public List<Producto> listarProductos() {
        return service.listarProductos();
    }

    // GET → obtener por ID ✅
    @GetMapping("/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return service.getProductoById(id);
    }

    // POST → crear producto ✅
    @PostMapping
    public @NonNull Producto crearProducto(@RequestBody Producto producto) {
        return service.guardarProducto(producto);
    }

    // PUT → actualizar producto ✅
    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return service.actualizarProducto(id, producto);
    }

    // DELETE → eliminación lógica ✅
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {
        service.eliminarLogico(id);
    }
}
