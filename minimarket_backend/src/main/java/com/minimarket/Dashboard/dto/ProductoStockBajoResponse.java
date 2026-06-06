package com.minimarket.Dashboard.dto;

import java.math.BigDecimal;

public class ProductoStockBajoResponse {
    private Long id;
    private String nombre;
    private Integer stock;
    private Integer stockMinimo;
    private String estado;
    private String categoria;
    private BigDecimal precio;

    // Constructor vacío
    public ProductoStockBajoResponse() {}

    // Constructor básico
    public ProductoStockBajoResponse(Long id, String nombre, Integer stock, Integer stockMinimo) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = determinarEstado(stock, stockMinimo);
    }
    
    // Constructor completo
    public ProductoStockBajoResponse(Long id, String nombre, Integer stock, Integer stockMinimo, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }
    
    // Constructor con categoría y precio
    public ProductoStockBajoResponse(Long id, String nombre, Integer stock, Integer stockMinimo, 
                                     String categoria, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
        this.precio = precio;
        this.estado = determinarEstado(stock, stockMinimo);
    }
    
    private String determinarEstado(Integer stock, Integer stockMinimo) {
        if (stock == 0) return "AGOTADO";
        if (stock <= stockMinimo) return "STOCK BAJO";
        return "NORMAL";
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}