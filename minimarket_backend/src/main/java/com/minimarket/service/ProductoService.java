package com.minimarket.service;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarProductos() {
        return repository.findAll();
    }

    public Producto getProductoById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    public @NonNull Producto guardarProducto(@NonNull Producto producto) {
        return Objects.requireNonNull(repository.save(producto), "El producto guardado no puede ser nulo");
    }

    public Producto actualizarProducto(Long id, Producto data) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        producto.setNombre(data.getNombre());
        producto.setDescripcion(data.getDescripcion());
        producto.setPrecio(data.getPrecio());
        producto.setCategoria(data.getCategoria());
        producto.setStock(data.getStock());
        producto.setStockMinimo(data.getStockMinimo());
        if (data.getImagen() != null) {
            producto.setImagen(data.getImagen());
        }
        
        return repository.save(producto);
    }

    public void eliminarLogico(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        repository.save(producto);
    }
}
