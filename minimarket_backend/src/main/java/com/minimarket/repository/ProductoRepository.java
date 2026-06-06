package com.minimarket.repository;

import com.minimarket.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Productos con stock bajo (excluyendo cero)
    @Query("SELECT p FROM Producto p WHERE p.stock > 0 AND p.stock <= p.stockMinimo")
    List<Producto> findProductosStockBajo();
    
    // Productos agotados (stock = 0)
    @Query("SELECT p FROM Producto p WHERE p.stock = 0")
    List<Producto> findProductosAgotados();
    
    // Todos los productos con problemas de stock (incluyendo cero)
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo ORDER BY p.stock ASC")
    List<Producto> findProductosConProblemasStock();
    
    // Productos con stock por debajo de un umbral específico
    @Query("SELECT p FROM Producto p WHERE p.stock <= :umbral")
    List<Producto> findProductosConStockMenorA(@Param("umbral") Integer umbral);
    
    // Contar productos por estado
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock > 0")
    Long countProductosConStock();
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock = 0")
    Long countProductosAgotados();
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock > 0 AND p.stock <= p.stockMinimo")
    Long countProductosStockBajo();
}